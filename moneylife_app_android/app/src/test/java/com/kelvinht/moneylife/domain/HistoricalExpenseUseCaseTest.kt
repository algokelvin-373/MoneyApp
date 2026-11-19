package com.kelvinht.moneylife.domain

import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.repository.DailyLimitProvider
import com.kelvinht.moneylife.domain.repository.ExpenseRepository
import com.kelvinht.moneylife.domain.repository.SavingsRepository
import com.kelvinht.moneylife.domain.usecase.AddExpenseUseCase
import com.kelvinht.moneylife.domain.usecase.DeleteExpenseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoricalExpenseUseCaseTest {
    @Test
    fun addingAndDeletingPastExpenseUpdatesSavings() =
        runBlocking {
            val transactionRunner = FakeTransactionRunner()
            val expenses = MemoryExpenseRepository(transactionRunner)
            val savings = MemorySavingsRepository(transactionRunner)
            val add = AddExpenseUseCase(expenses, savings, transactionRunner, 100_000L)
            val delete = DeleteExpenseUseCase(expenses, savings, transactionRunner, 100_000L)

            val id = add(ExpenseCategory.MAKAN, 30_000L, "Kemarin", "2020-01-01").getOrThrow()
            assertEquals("2020-01-01", expenses.getExpenseById(id)?.dateKey)
            assertEquals(70_000L, savings.saved["2020-01-01"])

            delete(id)
            assertEquals(null, expenses.getExpenseById(id))
            assertEquals(100_000L, savings.saved["2020-01-01"])
        }

    @Test
    fun pastExpenseUsesTheCurrentlySelectedLimit() =
        runBlocking {
            val transactionRunner = FakeTransactionRunner()
            val expenses = MemoryExpenseRepository(transactionRunner)
            val savings = MemorySavingsRepository(transactionRunner)
            var selectedLimit = 100_000L
            val add =
                AddExpenseUseCase(
                    expenses,
                    savings,
                    transactionRunner,
                    DailyLimitProvider { selectedLimit },
                )

            add(ExpenseCategory.MAKAN, 30_000L, "Kemarin", "2020-01-01").getOrThrow()
            assertEquals(70_000L, savings.saved["2020-01-01"])

            selectedLimit = 50_000L
            add(ExpenseCategory.MAKAN, 10_000L, "Kemarin", "2020-01-01").getOrThrow()
            assertEquals(10_000L, savings.saved["2020-01-01"])
        }

    @Test
    fun addingTodayDoesNotSaveUnfinishedBudget() =
        runBlocking {
            val transactionRunner = FakeTransactionRunner()
            val expenses = MemoryExpenseRepository(transactionRunner)
            val savings = MemorySavingsRepository(transactionRunner)

            val result =
                AddExpenseUseCase(expenses, savings, transactionRunner, 100_000L)(
                    ExpenseCategory.MAKAN,
                    30_000L,
                    "Hari ini",
                    DateUtils.todayKey(),
                )

            assertTrue(result.isSuccess)
            assertTrue(savings.saved.isEmpty())
        }

    @Test
    fun expenseAndSavingsAreWrittenInsideOneTransaction() =
        runBlocking {
            val transactionRunner = FakeTransactionRunner()
            val expenses = MemoryExpenseRepository(transactionRunner)
            val savings = MemorySavingsRepository(transactionRunner)
            val add = AddExpenseUseCase(expenses, savings, transactionRunner, 100_000L)

            add(ExpenseCategory.MAKAN, 30_000L, "Kemarin", "2020-01-01").getOrThrow()

            assertEquals(1, transactionRunner.transactions)
            assertEquals(listOf(true), expenses.writesInsideTransaction)
            assertEquals(listOf(true), savings.writesInsideTransaction)
        }

    @Test
    fun deletingAnExpenseRewritesTheSavingsInsideOneTransaction() =
        runBlocking {
            val transactionRunner = FakeTransactionRunner()
            val expenses = MemoryExpenseRepository(transactionRunner)
            val savings = MemorySavingsRepository(transactionRunner)
            val add = AddExpenseUseCase(expenses, savings, transactionRunner, 100_000L)
            val delete = DeleteExpenseUseCase(expenses, savings, transactionRunner, 100_000L)

            val id = add(ExpenseCategory.MAKAN, 30_000L, "Kemarin", "2020-01-01").getOrThrow()
            expenses.writesInsideTransaction.clear()
            savings.writesInsideTransaction.clear()
            val transactionsBeforeDelete = transactionRunner.transactions

            delete(id)

            assertEquals(transactionsBeforeDelete + 1, transactionRunner.transactions)
            assertEquals(listOf(true), expenses.writesInsideTransaction)
            assertEquals(listOf(true), savings.writesInsideTransaction)
        }

    @Test
    fun failingSavingsUpdateFailsTheWholeOperation() =
        runBlocking {
            val transactionRunner = FakeTransactionRunner()
            val expenses = MemoryExpenseRepository(transactionRunner)
            val savings = MemorySavingsRepository(transactionRunner, failOnSave = true)
            val add = AddExpenseUseCase(expenses, savings, transactionRunner, 100_000L)

            val result = add(ExpenseCategory.MAKAN, 30_000L, "Kemarin", "2020-01-01")

            assertTrue(result.isFailure)
        }
}

private class MemoryExpenseRepository(
    private val transactionRunner: FakeTransactionRunner,
) : ExpenseRepository {
    private val expenses = mutableListOf<Expense>()
    private var nextId = 1L
    val writesInsideTransaction = mutableListOf<Boolean>()

    override fun observeExpensesByDate(dateKey: String): Flow<List<Expense>> = MutableStateFlow(emptyList())

    override suspend fun getExpensesByDate(dateKey: String): List<Expense> = expenses.filter { it.dateKey == dateKey }

    override suspend fun getExpenseById(id: Long): Expense? = expenses.find { it.id == id }

    override suspend fun addExpense(expense: Expense): Long {
        writesInsideTransaction.add(transactionRunner.inTransaction)
        val id = nextId++
        expenses.add(expense.copy(id = id))
        return id
    }

    override suspend fun deleteExpenseById(id: Long) {
        writesInsideTransaction.add(transactionRunner.inTransaction)
        expenses.removeAll { it.id == id }
    }
}

private class MemorySavingsRepository(
    private val transactionRunner: FakeTransactionRunner,
    private val failOnSave: Boolean = false,
) : SavingsRepository {
    val saved = mutableMapOf<String, Long>()
    val writesInsideTransaction = mutableListOf<Boolean>()

    override fun observeBalance(monthKey: String): Flow<Long> = MutableStateFlow(saved.values.sum())

    override suspend fun saveDailySaving(
        dateKey: String,
        amount: Long,
    ) {
        writesInsideTransaction.add(transactionRunner.inTransaction)
        if (failOnSave) throw IllegalStateException("Gagal menyimpan tabungan")
        saved[dateKey] = amount
    }

    override suspend fun saveDailySavings(savings: Map<String, Long>) {
        writesInsideTransaction.add(transactionRunner.inTransaction)
        saved.putAll(savings)
    }

    override suspend fun getLastActiveDate(): String? = null

    override suspend fun setLastActiveDate(dateKey: String) = Unit
}
