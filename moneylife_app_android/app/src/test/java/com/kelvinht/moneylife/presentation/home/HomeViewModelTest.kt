package com.kelvinht.moneylife.presentation.home

import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.FakeTransactionRunner
import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.usecase.CalculateDailyBudgetUseCase
import com.kelvinht.moneylife.domain.usecase.DeleteExpenseUseCase
import com.kelvinht.moneylife.domain.usecase.GetDailyExpensesUseCase
import com.kelvinht.moneylife.domain.usecase.GetDailyLimitUseCase
import com.kelvinht.moneylife.domain.usecase.ObserveDailyLimitUseCase
import com.kelvinht.moneylife.domain.usecase.ObserveSavingsBalanceUseCase
import com.kelvinht.moneylife.domain.usecase.RolloverDailySavingsUseCase
import com.kelvinht.moneylife.domain.usecase.SetDailyLimitUseCase
import com.kelvinht.moneylife.domain.usecase.SnapshotDailyLimitUseCase
import com.kelvinht.moneylife.testing.FakeDailyLimitRepository
import com.kelvinht.moneylife.testing.FakeExpenseRepository
import com.kelvinht.moneylife.testing.FakeSavingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Guards the behaviour of the home screen ViewModel: date navigation, the
 * guard against future dates, the budget status and the savings rollover.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val expenseRepository = FakeExpenseRepository()
    private val savingsRepository = FakeSavingsRepository()
    private val dailyLimitRepository = FakeDailyLimitRepository()
    private val transactionRunner = FakeTransactionRunner()
    private lateinit var mainDispatcher: TestDispatcher

    @Before
    fun setUp() {
        mainDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_showsTodayWithoutExpenses() =
        runTest(mainDispatcher) {
            val viewModel = createViewModel()
            observe(viewModel)

            assertEquals(DateUtils.todayKey(), viewModel.uiState.value.dateKey)
            assertTrue(viewModel.uiState.value.isEmpty)
            assertEquals(BudgetStatus.SAFE, viewModel.uiState.value.budgetStatus)
        }

    @Test
    fun selectDate_inThePast_showsTheExpensesOfThatDay() =
        runTest(mainDispatcher) {
            val yesterday = dateKeyOfDays(-1)
            expenseRepository.seed(expense(30_000, yesterday))
            val viewModel = createViewModel()
            observe(viewModel)

            viewModel.selectDate(yesterday)
            advanceUntilIdle()

            assertEquals(yesterday, viewModel.uiState.value.dateKey)
            assertEquals(1, viewModel.uiState.value.expenses.size)
            assertEquals(30_000L, viewModel.uiState.value.spent)
            assertEquals(30_000L, viewModel.uiState.value.spentByCategory[ExpenseCategory.MAKAN])
        }

    @Test
    fun selectDate_inTheFuture_isIgnored() =
        runTest(mainDispatcher) {
            val viewModel = createViewModel()
            observe(viewModel)

            viewModel.selectDate(dateKeyOfDays(1))
            advanceUntilIdle()

            assertEquals(DateUtils.todayKey(), viewModel.uiState.value.dateKey)
        }

    @Test
    fun nextDay_fromToday_isIgnored() =
        runTest(mainDispatcher) {
            val viewModel = createViewModel()
            observe(viewModel)

            viewModel.nextDay()
            advanceUntilIdle()

            assertEquals(DateUtils.todayKey(), viewModel.uiState.value.dateKey)
        }

    @Test
    fun previousDay_movesOneDayBack() =
        runTest(mainDispatcher) {
            val viewModel = createViewModel()
            observe(viewModel)

            viewModel.previousDay()
            advanceUntilIdle()

            assertEquals(dateKeyOfDays(-1), viewModel.uiState.value.dateKey)
        }

    @Test
    fun showToday_afterNavigatingBack_returnsToToday() =
        runTest(mainDispatcher) {
            val viewModel = createViewModel()
            observe(viewModel)

            viewModel.previousDay()
            advanceUntilIdle()
            viewModel.showToday()
            advanceUntilIdle()

            assertEquals(DateUtils.todayKey(), viewModel.uiState.value.dateKey)
        }

    @Test
    fun budgetStatus_isWarningWhenUsageReachesEightyPercent() =
        runTest(mainDispatcher) {
            expenseRepository.seed(expense(80_000, DateUtils.todayKey()))
            val viewModel = createViewModel()
            observe(viewModel)

            assertEquals(BudgetStatus.WARNING, viewModel.uiState.value.budgetStatus)
            assertEquals(80, viewModel.uiState.value.usagePercent)
        }

    @Test
    fun budgetStatus_isOverWhenSpendingExceedsTheLimit() =
        runTest(mainDispatcher) {
            expenseRepository.seed(expense(150_000, DateUtils.todayKey()))
            val viewModel = createViewModel()
            observe(viewModel)

            assertEquals(BudgetStatus.OVER, viewModel.uiState.value.budgetStatus)
            assertEquals(100, viewModel.uiState.value.usagePercent)
            assertTrue(viewModel.uiState.value.overSpent > 0L)
        }

    @Test
    fun refresh_settlesEveryMissedDay() =
        runTest(mainDispatcher) {
            val threeDaysAgo = dateKeyOfDays(-3)
            savingsRepository.lastActiveDate = threeDaysAgo
            expenseRepository.seed(expense(30_000, threeDaysAgo))
            val viewModel = createViewModel()
            observe(viewModel)

            viewModel.refresh()
            advanceUntilIdle()

            val saved = savingsRepository.savedSavings()
            assertEquals(70_000L, saved[threeDaysAgo])
            assertEquals(100_000L, saved[dateKeyOfDays(-2)])
            assertEquals(100_000L, saved[dateKeyOfDays(-1)])
            assertEquals(DateUtils.todayKey(), savingsRepository.lastActiveDate)
        }

    @Test
    fun refresh_keepsTheDaySelectedByTheUser() =
        runTest(mainDispatcher) {
            val yesterday = dateKeyOfDays(-1)
            val viewModel = createViewModel()
            observe(viewModel)

            viewModel.selectDate(yesterday)
            advanceUntilIdle()
            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(yesterday, viewModel.uiState.value.dateKey)
        }

    @Test
    fun deleteExpense_removesTheExpenseFromTheList() =
        runTest(mainDispatcher) {
            val today = DateUtils.todayKey()
            expenseRepository.seed(expense(25_000, today))
            val viewModel = createViewModel()
            observe(viewModel)
            val id = expenseRepository.current().first().id

            viewModel.deleteExpense(id)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.expenses.isEmpty())
            assertTrue(viewModel.uiState.value.isEmpty)
            assertEquals(0L, viewModel.uiState.value.spent)
        }

    @Test
    fun selectDailyLimit_updatesTheBudgetImmediately() =
        runTest(mainDispatcher) {
            expenseRepository.seed(expense(40_000, DateUtils.todayKey()))
            val viewModel = createViewModel()
            observe(viewModel)

            viewModel.selectDailyLimit(50_000L)
            advanceUntilIdle()

            assertEquals(50_000L, viewModel.uiState.value.limit)
            assertEquals(10_000L, viewModel.uiState.value.remaining)
            assertEquals(80, viewModel.uiState.value.usagePercent)
            assertEquals(BudgetStatus.WARNING, viewModel.uiState.value.budgetStatus)
        }

    @Test
    fun startsWithThePersistedDailyLimit() =
        runTest(mainDispatcher) {
            val limitRepository = FakeDailyLimitRepository(50_000L)
            val viewModel = createViewModel(limitRepository)
            observe(viewModel)

            assertEquals(50_000L, viewModel.uiState.value.limit)
        }

    @Test
    fun refresh_settlesMissedDaysWithTheSelectedLimit() =
        runTest(mainDispatcher) {
            val yesterday = dateKeyOfDays(-1)
            savingsRepository.lastActiveDate = yesterday
            val viewModel = createViewModel(FakeDailyLimitRepository(50_000L))
            observe(viewModel)

            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(50_000L, savingsRepository.savedSavings()[yesterday])
        }

    @Test
    fun pastDay_keepsItsLimitAndIgnoresChanges() =
        runTest(mainDispatcher) {
            val yesterday = dateKeyOfDays(-1)
            savingsRepository.lastActiveDate = yesterday
            val viewModel = createViewModel()
            observe(viewModel)

            // Opening the app settles and locks yesterday at the old limit.
            viewModel.refresh()
            advanceUntilIdle()

            viewModel.selectDailyLimit(50_000L)
            advanceUntilIdle()
            viewModel.selectDate(yesterday)
            advanceUntilIdle()

            assertEquals(100_000L, viewModel.uiState.value.limit)

            viewModel.selectDailyLimit(50_000L)
            advanceUntilIdle()

            assertEquals(100_000L, viewModel.uiState.value.limit)
        }

    private fun createViewModel(limitRepository: FakeDailyLimitRepository = dailyLimitRepository): HomeViewModel =
        HomeViewModel(
            getDailyExpenses = GetDailyExpensesUseCase(expenseRepository),
            calculateDailyBudget = CalculateDailyBudgetUseCase(),
            deleteExpense = DeleteExpenseUseCase(expenseRepository, savingsRepository, transactionRunner, DAILY_LIMIT),
            observeSavingsBalance = ObserveSavingsBalanceUseCase(savingsRepository),
            rolloverDailySavings =
                RolloverDailySavingsUseCase(
                    expenseRepository,
                    savingsRepository,
                    transactionRunner,
                ),
            getDailyLimit = GetDailyLimitUseCase(limitRepository),
            observeDailyLimit = ObserveDailyLimitUseCase(limitRepository),
            setDailyLimit = SetDailyLimitUseCase(limitRepository),
            snapshotDailyLimit = SnapshotDailyLimitUseCase(limitRepository),
        )

    /** Keeps the state flow alive so the ViewModel keeps producing updates. */
    private fun TestScope.observe(viewModel: HomeViewModel) {
        backgroundScope.launch { viewModel.uiState.collect { } }
    }

    private fun dateKeyOfDays(offset: Int): String = DateUtils.dateKey(DateUtils.parseDate(DateUtils.todayKey()).plusDays(offset.toLong()))

    private fun expense(
        amount: Long,
        dateKey: String,
    ) = Expense(
        category = ExpenseCategory.MAKAN,
        amount = amount,
        note = "Uji",
        dateKey = dateKey,
        createdAt = 0L,
    )

    private companion object {
        const val DAILY_LIMIT = 100_000L
    }
}
