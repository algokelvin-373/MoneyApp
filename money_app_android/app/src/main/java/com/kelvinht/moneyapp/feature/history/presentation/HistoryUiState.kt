package com.kelvinht.moneyapp.feature.history.presentation

import com.kelvinht.moneyapp.feature.history.domain.model.HistoryTransactionType

data class HistoryUiState(
    val monthLabel: String,
    val totalIncome: String,
    val incomeCount: String,
    val totalExpense: String,
    val expenseCount: String,
    val netCashFlow: String,
    val isSurplus: Boolean,
    val incomeRatio: Float,
    val groups: List<HistoryGroupUi>,
    val displayInfo: String,
    val hasResults: Boolean
)

data class HistoryGroupUi(val dateLabel: String, val dailyTotal: String, val transactions: List<HistoryTransactionUi>)

data class HistoryTransactionUi(
    val title: String,
    val category: String,
    val accountAndTime: String,
    val amount: String,
    val isIncome: Boolean,
    val type: HistoryTransactionType
)
