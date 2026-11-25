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
        val categoryTotals = ExpenseCategory.entries.associateWith { category ->
            expenses.filter { it.category == category }.sumOf { it.amount }
        }

        return MonthlyExpenseSummary(
            totalExpenses = expenses.sumOf { it.amount },
            categoryTotals = categoryTotals,
            totalSavings = savingsRepository.getMonthlySavings(monthKey),
        )
    }
}
