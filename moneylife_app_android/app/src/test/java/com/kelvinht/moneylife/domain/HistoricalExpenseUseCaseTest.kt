package com.kelvinht.moneylife.domain

import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.model.ExpenseCategory
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
    fun addingAndDeletingPastExpenseUpdatesSavings() = runBlocking {
        val expenses = MemoryExpenseRepository()
        val savings = MemorySavingsRepository()
        val add = AddExpenseUseCase(expenses, savings, 100_000L)
        val delete = DeleteExpenseUseCase(expenses, savings, 100_000L)

        val id = add(ExpenseCategory.MAKAN, 30_000L, "Kemarin", "2020-01-01").getOrThrow()
        assertEquals("2020-01-01", expenses.getExpenseById(id)?.dateKey)
        assertEquals(70_000L, savings.saved["2020-01-01"])

        delete(id)
        assertEquals(null, expenses.getExpenseById(id))
        assertEquals(100_000L, savings.saved["2020-01-01"])
    }

    @Test
    fun addingTodayDoesNotSaveUnfinishedBudget() = runBlocking {
        val expenses = MemoryExpenseRepository()
        val savings = MemorySavingsRepository()

        val result = AddExpenseUseCase(expenses, savings, 100_000L)(
            ExpenseCategory.MAKAN, 30_000L, "Hari ini", DateUtils.todayKey()
        )

        assertTrue(result.isSuccess)
        assertTrue(savings.saved.isEmpty())
    }
}

private class MemoryExpenseRepository : ExpenseRepository {
    private val expenses = mutableListOf<Expense>()
    private var nextId = 1L

    override fun observeExpensesByDate(dateKey: String): Flow<List<Expense>> = MutableStateFlow(emptyList())
    override suspend fun getExpensesByDate(dateKey: String): List<Expense> =
        expenses.filter { it.dateKey == dateKey }
    override suspend fun getExpenseById(id: Long): Expense? = expenses.find { it.id == id }
    override suspend fun addExpense(expense: Expense): Long {
        val id = nextId++
        expenses.add(expense.copy(id = id))
        return id
    }
    override suspend fun deleteExpenseById(id: Long) {
        expenses.removeAll { it.id == id }
    }
}

private class MemorySavingsRepository : SavingsRepository {
    val saved = mutableMapOf<String, Long>()
    override fun observeBalance(monthKey: String): Flow<Long> = MutableStateFlow(saved.values.sum())
    override suspend fun saveDailySaving(dateKey: String, amount: Long) {
        saved[dateKey] = amount
    }

    override suspend fun saveDailySavings(savings: Map<String, Long>) {
        saved.putAll(savings)
    }
    override suspend fun getLastActiveDate(): String? = null
    override suspend fun setLastActiveDate(dateKey: String) = Unit
}
