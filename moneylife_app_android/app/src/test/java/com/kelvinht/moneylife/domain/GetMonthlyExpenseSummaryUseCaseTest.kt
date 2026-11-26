package com.kelvinht.moneylife.domain

import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.usecase.GetMonthlyExpenseSummaryUseCase
import com.kelvinht.moneylife.testing.FakeExpenseRepository
import com.kelvinht.moneylife.testing.FakeSavingsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMonthlyExpenseSummaryUseCaseTest {
    @Test
    fun aggregatesMonthlyTotalsByCategoryAndDay() =
        runBlocking {
            val expenses = FakeExpenseRepository()
            expenses.seed(
                expense(ExpenseCategory.MAKAN, 10_000L, "2026-11-01", 1L),
                expense(ExpenseCategory.MAKAN, 5_000L, "2026-11-02", 2L),
                expense(ExpenseCategory.JAJAN, 3_000L, "2026-12-01", 3L),
            )
            val savings = FakeSavingsRepository()
            savings.saveDailySaving("2026-11-01", 7_000L)

            val summary = GetMonthlyExpenseSummaryUseCase(expenses, savings)("2026-11")

            assertEquals(15_000L, summary.totalExpenses)
            assertEquals(15_000L, summary.categoryTotals[ExpenseCategory.MAKAN])
            assertEquals(0L, summary.categoryTotals[ExpenseCategory.JAJAN])
            assertEquals(mapOf("2026-11-01" to 10_000L, "2026-11-02" to 5_000L), summary.dailyTotals)
            assertEquals(7_000L, summary.totalSavings)
        }

    private fun expense(
        category: ExpenseCategory,
        amount: Long,
        dateKey: String,
        createdAt: Long,
    ): Expense {
        return Expense(
            category = category,
            amount = amount,
            dateKey = dateKey,
            createdAt = createdAt,
        )
    }
}
