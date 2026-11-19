package com.kelvinht.moneyapp.feature.home.presentation

import androidx.lifecycle.ViewModel
import com.kelvinht.moneyapp.core.format.RupiahFormatter
import com.kelvinht.moneyapp.feature.home.domain.model.BudgetCategoryType
import com.kelvinht.moneyapp.feature.home.domain.usecase.GetHomeSnapshotUseCase

class HomeViewModel(private val getHomeSnapshot: GetHomeSnapshotUseCase) : ViewModel() {
    fun loadHome(): HomeUiState {
        val snapshot = getHomeSnapshot()
        val progress = percentage(snapshot.dailySpent, snapshot.dailyLimit)
        return HomeUiState(
            dateLabel = snapshot.dateLabel,
            spent = RupiahFormatter.format(snapshot.dailySpent),
            usageLabel = progress.toString() + "% Terpakai",
            limit = RupiahFormatter.format(snapshot.dailyLimit),
            remaining = RupiahFormatter.format((snapshot.dailyLimit - snapshot.dailySpent).coerceAtLeast(0)),
            averageDaily = RupiahFormatter.format(snapshot.averageDaily),
            budgetCondition = snapshot.budgetCondition,
            progress = progress,
            categories = snapshot.categories.map { category ->
                BudgetCategoryUi(
                    name = category.name,
                    amountLabel = compact(category.spent) + " / " + compact(category.limit),
                    progress = percentage(category.spent, category.limit),
                    style = when (category.type) {
                        BudgetCategoryType.FOOD -> BudgetCategoryStyle.FOOD
                        BudgetCategoryType.TRANSPORT -> BudgetCategoryStyle.TRANSPORT
                        BudgetCategoryType.SNACK -> BudgetCategoryStyle.SNACK
                    }
                )
            },
            isEmpty = snapshot.todayTransactionCount == 0
        )
    }

    private fun percentage(value: Long, limit: Long): Int =
        if (limit <= 0) 0 else ((value * 100) / limit).toInt().coerceIn(0, 100)

    private fun compact(value: Long): String = when {
        value == 0L -> "Rp 0"
        value % 1_000_000 == 0L -> "Rp " + (value / 1_000_000) + "jt"
        value % 1_000 == 0L -> "Rp " + (value / 1_000) + "k"
        else -> RupiahFormatter.format(value)
    }
}
