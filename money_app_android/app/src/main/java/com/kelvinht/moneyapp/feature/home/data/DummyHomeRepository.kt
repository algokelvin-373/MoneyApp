package com.kelvinht.moneyapp.feature.home.data

import com.kelvinht.moneyapp.feature.home.domain.model.BudgetCategoryType
import com.kelvinht.moneyapp.feature.home.domain.model.HomeBudgetCategory
import com.kelvinht.moneyapp.feature.home.domain.model.HomeSnapshot
import com.kelvinht.moneyapp.feature.home.domain.repository.HomeRepository

class DummyHomeRepository : HomeRepository {
    override fun getHomeSnapshot() = HomeSnapshot(
        dateLabel = "Jumat, 9 Okt 2026",
        dailySpent = 0,
        dailyLimit = 100_000,
        averageDaily = 45_000,
        budgetCondition = "Sangat Aman",
        categories = listOf(
            HomeBudgetCategory("Makan", 0, 50_000, BudgetCategoryType.FOOD),
            HomeBudgetCategory("Transport", 0, 25_000, BudgetCategoryType.TRANSPORT),
            HomeBudgetCategory("Jajan", 0, 25_000, BudgetCategoryType.SNACK)
        ),
        todayTransactionCount = 0
    )
}
