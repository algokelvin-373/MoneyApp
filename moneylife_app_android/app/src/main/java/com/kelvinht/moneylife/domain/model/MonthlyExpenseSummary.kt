package com.kelvinht.moneylife.domain.model

data class MonthlyExpenseSummary(
    val totalExpenses: Long,
    val categoryTotals: Map<ExpenseCategory, Long>,
    val dailyTotals: Map<String, Long>,
    val totalSavings: Long,
)
