package com.kelvinht.moneyapp.feature.home.domain.model

data class HomeSnapshot(
    val dateLabel: String,
    val dailySpent: Long,
    val dailyLimit: Long,
    val averageDaily: Long,
    val budgetCondition: String,
    val categories: List<HomeBudgetCategory>,
    val todayTransactionCount: Int
)

data class HomeBudgetCategory(
    val name: String,
    val spent: Long,
    val limit: Long,
    val type: BudgetCategoryType
)

enum class BudgetCategoryType { FOOD, TRANSPORT, SNACK }
