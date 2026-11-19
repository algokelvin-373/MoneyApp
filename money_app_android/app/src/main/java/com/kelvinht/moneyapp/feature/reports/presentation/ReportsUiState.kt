package com.kelvinht.moneyapp.feature.reports.presentation

data class ReportsUiState(
    val totalExpenseCompact: String,
    val categories: List<ExpenseCategoryUi>,
    val remainingBudget: String,
    val totalBudget: String,
    val usedBudget: String,
    val budgetProgress: Int,
    val budgetStatus: String,
    val weeklyTipTitle: String,
    val weeklyTipDescription: String
)

data class ExpenseCategoryUi(
    val name: String,
    val percentageLabel: String,
    val percentage: Int,
    val style: ExpenseCategoryUiStyle
)

enum class ExpenseCategoryUiStyle {
    FOOD,
    TRANSPORT,
    SAVING
}
