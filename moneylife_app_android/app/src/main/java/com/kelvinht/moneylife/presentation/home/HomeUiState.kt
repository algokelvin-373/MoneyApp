package com.kelvinht.moneylife.presentation.home

import com.kelvinht.moneylife.presentation.common.CategoryStyle

enum class BudgetStatus {
    SAFE,
    WARNING,
    OVER
}

data class ExpenseUiModel(
    val id: Long,
    val style: CategoryStyle,
    val note: String,
    val amountText: String,
    val timeText: String
)

data class HomeUiState(
    val dateLabel: String = "",
    val spentText: String = "Rp 0",
    val limitText: String = "",
    val remainingText: String = "",
    val overLimitText: String? = null,
    val usagePercent: Int = 0,
    val usageText: String = "0% Terpakai",
    val averageText: String = "Rp 0",
    val conditionText: String = "Sangat Aman",
    val makanText: String = "Rp 0/50k",
    val transportText: String = "Rp 0/30k",
    val jajanText: String = "Rp 0/20k",
    val budgetStatus: BudgetStatus = BudgetStatus.SAFE,
    val expenses: List<ExpenseUiModel> = emptyList(),
    val isEmpty: Boolean = true
)
