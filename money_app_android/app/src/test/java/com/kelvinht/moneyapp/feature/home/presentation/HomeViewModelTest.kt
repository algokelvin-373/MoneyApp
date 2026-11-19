package com.kelvinht.moneyapp.feature.home.presentation

import com.kelvinht.moneyapp.feature.home.domain.model.BudgetCategoryType
import com.kelvinht.moneyapp.feature.home.domain.model.HomeBudgetCategory
import com.kelvinht.moneyapp.feature.home.domain.model.HomeSnapshot
import com.kelvinht.moneyapp.feature.home.domain.repository.HomeRepository
import com.kelvinht.moneyapp.feature.home.domain.usecase.GetHomeSnapshotUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelTest {
    @Test
    fun loadHome_mapsDailyBudgetIntoDisplayState() {
        val state = HomeViewModel(GetHomeSnapshotUseCase(FakeHomeRepository())).loadHome()

        assertEquals("Jumat, 9 Okt 2026", state.dateLabel)
        assertEquals("Rp 25.000", state.spent)
        assertEquals("25% Terpakai", state.usageLabel)
        assertEquals("Rp 75.000", state.remaining)
        assertEquals(25, state.progress)
        assertEquals("Rp 10k / Rp 50k", state.categories.first().amountLabel)
        assertEquals(20, state.categories.first().progress)
        assertTrue(state.isEmpty)
    }
}

private class FakeHomeRepository : HomeRepository {
    override fun getHomeSnapshot() = HomeSnapshot(
        dateLabel = "Jumat, 9 Okt 2026",
        dailySpent = 25_000,
        dailyLimit = 100_000,
        averageDaily = 45_000,
        budgetCondition = "Aman",
        categories = listOf(HomeBudgetCategory("Makan", 10_000, 50_000, BudgetCategoryType.FOOD)),
        todayTransactionCount = 0
    )
}
