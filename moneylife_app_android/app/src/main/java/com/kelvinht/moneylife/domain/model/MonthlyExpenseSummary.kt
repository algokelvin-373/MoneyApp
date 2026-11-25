package com.kelvinht.moneylife.domain.model

data class MonthlyExpenseSummary(
    val totalExpenses: Long,
    val categoryTotals: Map<ExpenseCategory, Long>,
    val totalSavings: Long,
)
