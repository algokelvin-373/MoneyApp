package com.kelvinht.moneylife.presentation.addedit

import androidx.lifecycle.SavedStateHandle
import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.FakeTransactionRunner
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.repository.DailyLimitProvider
import com.kelvinht.moneylife.domain.usecase.AddExpenseUseCase
import com.kelvinht.moneylife.domain.usecase.GetExpenseByIdUseCase
import com.kelvinht.moneylife.domain.usecase.UpdateExpenseUseCase
import com.kelvinht.moneylife.testing.FakeExpenseRepository
import com.kelvinht.moneylife.testing.FakeSavingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Guards the form behaviour of the add expense screen: amount validation, the
 * quick amount chips, resetting the form and the error path of the save.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddExpenseViewModelTest {
    private val expenseRepository = FakeExpenseRepository()
    private val savingsRepository = FakeSavingsRepository()
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
    fun initialState_hasEmptyAmountAndNote() =
        runTest(mainDispatcher) {
            val viewModel = createViewModel()

            assertEquals("", viewModel.uiState.value.amountInput)
            assertEquals("", viewModel.uiState.value.note)
            assertEquals(DateUtils.todayKey(), viewModel.uiState.value.dateKey)
        }

    @Test
    fun save_withoutAmount_showsAnErrorAndStoresNothing() =
        runTest(mainDispatcher) {
            val viewModel = createViewModel()

            viewModel.save()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)
            assertFalse(viewModel.uiState.value.isSaved)
            assertTrue(expenseRepository.current().isEmpty())
        }

    @Test
    fun save_withZeroAmount_showsAnError() =
        runTest(mainDispatcher) {
            val viewModel = createViewModel()
            viewModel.onAmountChanged("0")

            viewModel.save()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)
            assertTrue(expenseRepository.current().isEmpty())
        }

    @Test
    fun save_withAValidAmount_storesTheExpense() =
        runTest(mainDispatcher) {
            val viewModel = createViewModel()
            viewModel.selectCategory(ExpenseCategory.JAJAN)
            viewModel.onAmountChanged("25000")
            viewModel.onNoteChanged("Kopi")

            viewModel.save()
            advanceUntilIdle()

            val stored = expenseRepository.current()
            assertEquals(1, stored.size)
            assertEquals(25_000L, stored.first().amount)
            assertEquals(ExpenseCategory.JAJAN, stored.first().category)
            assertEquals("Kopi", stored.first().note)
            assertTrue(viewModel.uiState.value.isSaved)
            assertFalse(viewModel.uiState.value.isSaving)
        }

    @Test
    fun save_supportsEveryNewExpenseCategory() =
        runTest(mainDispatcher) {
            val newCategories =
                listOf(
                    ExpenseCategory.KEBUTUHAN_LAIN,
                    ExpenseCategory.KEBUTUHAN_ORANG_TUA,
                    ExpenseCategory.TOP_UP_COINS_TIKTOK,
                )

            newCategories.forEachIndexed { index, category ->
                val viewModel = createViewModel()
                viewModel.selectCategory(category)
                viewModel.onAmountChanged((10_000L + index).toString())
                viewModel.save()
                advanceUntilIdle()
            }

            assertEquals(newCategories.toSet(), expenseRepository.current().map { it.category }.toSet())
        }

    @Test
    fun save_thatFails_showsAnErrorAndKeepsSavingFalse() =
        runTest(mainDispatcher) {
            expenseRepository.failOnWrite = true
            val viewModel = createViewModel()
            viewModel.onAmountChanged("25000")

            viewModel.save()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)
            assertFalse(viewModel.uiState.value.isSaved)
            assertFalse(viewModel.uiState.value.isSaving)
        }

    @Test
    fun onAmountChanged_keepsOnlyDigits() =
        runTest(mainDispatcher) {
            val viewModel = createViewModel()

            viewModel.onAmountChanged("12a.500")

            assertEquals("12500", viewModel.uiState.value.amountInput)
        }

    @Test
    fun addQuickAmount_addsToTheCurrentAmount() =
        runTest(mainDispatcher) {
            val viewModel = createViewModel()
            viewModel.onAmountChanged("5000")

            viewModel.addQuickAmount(20_000)

            assertEquals("25000", viewModel.uiState.value.amountInput)
        }

    @Test
    fun addQuickAmount_capsAtTheMaximumAmount() =
        runTest(mainDispatcher) {
            val viewModel = createViewModel()
            viewModel.onAmountChanged("999999999")

            viewModel.addQuickAmount(100_000)

            assertEquals("999999999", viewModel.uiState.value.amountInput)
        }

    @Test
    fun reset_clearsAmountAndNoteButKeepsTheDateAndCategory() =
        runTest(mainDispatcher) {
            val yesterday = dateKeyOfDays(-1)
            val viewModel = createViewModel()
            viewModel.selectCategory(ExpenseCategory.JAJAN)
            viewModel.selectDate(yesterday)
            viewModel.onAmountChanged("25000")
            viewModel.onNoteChanged("Kopi")

            viewModel.reset()

            assertEquals("", viewModel.uiState.value.amountInput)
            assertEquals("", viewModel.uiState.value.note)
            assertEquals(yesterday, viewModel.uiState.value.dateKey)
            assertEquals(ExpenseCategory.JAJAN, viewModel.uiState.value.category)
            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun selectDate_inThePast_isApplied() =
        runTest(mainDispatcher) {
            val yesterday = dateKeyOfDays(-1)
            val viewModel = createViewModel()

            viewModel.selectDate(yesterday)

            assertEquals(yesterday, viewModel.uiState.value.dateKey)
        }

    @Test
    fun selectDate_inTheFuture_isIgnored() =
        runTest(mainDispatcher) {
            val viewModel = createViewModel()

            viewModel.selectDate(dateKeyOfDays(1))

            assertEquals(DateUtils.todayKey(), viewModel.uiState.value.dateKey)
        }

    @Test
    fun formState_isRestoredFromSavedStateHandle() =
        runTest(mainDispatcher) {
            val handle = SavedStateHandle()
            val yesterday = dateKeyOfDays(-1)
            val first = createViewModel(handle)
            first.selectCategory(ExpenseCategory.JAJAN)
            first.selectDate(yesterday)
            first.onAmountChanged("25a000")
            first.onNoteChanged("Kopi")

            val recreated = createViewModel(handle)

            assertEquals(ExpenseCategory.JAJAN, recreated.uiState.value.category)
            assertEquals(yesterday, recreated.uiState.value.dateKey)
            assertEquals("25000", recreated.uiState.value.amountInput)
            assertEquals("Kopi", recreated.uiState.value.note)
        }

    @Test
    fun restoredFutureDate_fallsBackToToday() =
        runTest(mainDispatcher) {
            // A saved form from before the device clock was corrected must not
            // reopen on a day that is in the future.
            val handle = SavedStateHandle(mapOf("date" to dateKeyOfDays(1)))

            val viewModel = createViewModel(handle)

            assertEquals(DateUtils.todayKey(), viewModel.uiState.value.dateKey)
        }

    @Test
    fun restoredMalformedDate_fallsBackToToday() =
        runTest(mainDispatcher) {
            val handle = SavedStateHandle(mapOf("date" to "bukan-tanggal"))

            val viewModel = createViewModel(handle)

            assertEquals(DateUtils.todayKey(), viewModel.uiState.value.dateKey)
        }

    private fun createViewModel(handle: SavedStateHandle = SavedStateHandle()): AddExpenseViewModel =
        AddExpenseViewModel(
            addExpense =
                AddExpenseUseCase(
                    repository = expenseRepository,
                    savingsRepository = savingsRepository,
                    transactionRunner = transactionRunner,
                    dailyLimit = DAILY_LIMIT,
                ),
            getExpenseById = GetExpenseByIdUseCase(expenseRepository),
            updateExpense =
                UpdateExpenseUseCase(
                    expenseRepository,
                    savingsRepository,
                    transactionRunner,
                    DailyLimitProvider { DAILY_LIMIT },
                ),
            savedStateHandle = handle,
        )

    private fun dateKeyOfDays(offset: Int): String = DateUtils.dateKey(DateUtils.parseDate(DateUtils.todayKey()).plusDays(offset.toLong()))

    private companion object {
        const val DAILY_LIMIT = 100_000L
    }
}
