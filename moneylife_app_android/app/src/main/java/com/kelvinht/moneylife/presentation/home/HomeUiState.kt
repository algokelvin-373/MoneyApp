package com.kelvinht.moneylife.presentation.home

import com.kelvinht.moneylife.domain.model.DailyBudget
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.presentation.common.CategoryStyle

enum class BudgetStatus {
    SAFE,
    WARNING,
    OVER,
}

data class ExpenseUiModel(
    val id: Long,
    val style: CategoryStyle,
    val note: String,
    val amount: Long,
    val createdAt: Long,
)

/**
 * Raw values for the home screen. Formatting (rupiah, percentages and labels)
 * happens in [MainActivity], so the state stays free of ready-made display
 * strings and is easy to assert in tests.
 */
data class HomeUiState(
    val dateKey: String = "",
    val spent: Long = 0L,
    val limit: Long = DailyBudget.DEFAULT_DAILY_LIMIT,
    val remaining: Long = DailyBudget.DEFAULT_DAILY_LIMIT,
    val overSpent: Long = 0L,
    val usagePercent: Int = 0,
    val savingsBalance: Long = 0L,
    val budgetStatus: BudgetStatus = BudgetStatus.SAFE,
    val spentByCategory: Map<ExpenseCategory, Long> = emptyMap(),
    val expenses: List<ExpenseUiModel> = emptyList(),
    val isEmpty: Boolean = true,
)
