package com.kelvinht.moneyapp.feature.reports.domain.model

data class ReportsSnapshot(
    val totalExpense: Long,
    val categories: List<ExpenseCategory>,
    val remainingBudget: Long,
    val totalBudget: Long,
    val usedBudget: Long,
    val budgetStatus: String,
    val weeklyTipTitle: String,
    val weeklyTipDescription: String
)

data class ExpenseCategory(
    val name: String,
    val percentage: Int,
    val style: ExpenseCategoryStyle
)

enum class ExpenseCategoryStyle {
    FOOD,
    TRANSPORT,
    SAVING
}
