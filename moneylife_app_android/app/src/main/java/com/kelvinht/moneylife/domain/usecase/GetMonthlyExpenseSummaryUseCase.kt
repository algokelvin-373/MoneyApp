package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.model.MonthlyExpenseSummary
import com.kelvinht.moneylife.domain.repository.ExpenseRepository
import com.kelvinht.moneylife.domain.repository.SavingsRepository

class GetMonthlyExpenseSummaryUseCase(
    private val expenseRepository: ExpenseRepository,
    private val savingsRepository: SavingsRepository,
) {
    suspend operator fun invoke(monthKey: String): MonthlyExpenseSummary {
        val expenses = expenseRepository.getExpensesByMonth(monthKey)
        val categoryTotals = ExpenseCategory.entries.associateWith { 0L }.toMutableMap()
        expenses.forEach { expense ->
            categoryTotals[expense.category] = categoryTotals.getValue(expense.category) + expense.amount
        }
        val dailyTotals =
            expenses.groupingBy { it.dateKey }
                .fold(0L) { total, expense -> total + expense.amount }
                .toSortedMap()

        return MonthlyExpenseSummary(
            totalExpenses = expenses.sumOf { it.amount },
            categoryTotals = categoryTotals,
            dailyTotals = dailyTotals,
            totalSavings = savingsRepository.getMonthlySavings(monthKey),
        )
    }
}
