package com.kelvinht.moneylife.presentation.addedit

import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.FakeTransactionRunner
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.usecase.AddExpenseUseCase
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
    fun reset_clearsAmountAndNoteButKeepsTheDate() =
        runTest(mainDispatcher) {
            val yesterday = dateKeyOfDays(-1)
            val viewModel = createViewModel()
            viewModel.selectDate(yesterday)
            viewModel.onAmountChanged("25000")
            viewModel.onNoteChanged("Kopi")

            viewModel.reset()

            assertEquals("", viewModel.uiState.value.amountInput)
            assertEquals("", viewModel.uiState.value.note)
            assertEquals(yesterday, viewModel.uiState.value.dateKey)
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

    private fun createViewModel(): AddExpenseViewModel =
        AddExpenseViewModel(
            addExpense =
                AddExpenseUseCase(
                    repository = expenseRepository,
                    savingsRepository = savingsRepository,
                    transactionRunner = transactionRunner,
                    dailyLimit = DAILY_LIMIT,
                ),
        )

    private fun dateKeyOfDays(offset: Int): String = DateUtils.dateKey(DateUtils.parseDate(DateUtils.todayKey()).plusDays(offset.toLong()))

    private companion object {
        const val DAILY_LIMIT = 100_000L
    }
}
