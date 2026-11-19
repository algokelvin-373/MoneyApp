package com.kelvinht.moneyapp.feature.home.presentation

data class HomeUiState(
    val dateLabel: String,
    val spent: String,
    val usageLabel: String,
    val limit: String,
    val remaining: String,
    val averageDaily: String,
    val budgetCondition: String,
    val progress: Int,
    val categories: List<BudgetCategoryUi>,
    val isEmpty: Boolean
)

data class BudgetCategoryUi(
    val name: String,
    val amountLabel: String,
    val progress: Int,
    val style: BudgetCategoryStyle
)

enum class BudgetCategoryStyle { FOOD, TRANSPORT, SNACK }
