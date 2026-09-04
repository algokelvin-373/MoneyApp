package com.kelvinht.moneyapp.feature.history.domain.model

data class HistorySnapshot(
    val monthLabel: String,
    val totalIncome: Long,
    val incomeTransactionCount: Int,
    val totalExpense: Long,
    val expenseTransactionCount: Int,
    val displayedTransactionCount: Int,
    val totalTransactionCount: Int,
    val groups: List<HistoryGroup>
)

data class HistoryGroup(
    val dateLabel: String,
    val dailyTotal: Long,
    val transactions: List<HistoryTransaction>
)

data class HistoryTransaction(
    val title: String,
    val category: String,
    val account: String,
    val time: String,
    val amount: Long,
    val type: HistoryTransactionType
)

enum class HistoryTransactionType { FOOD, TRANSPORT, INCOME, TRANSFER, SHOPPING, CASHBACK }
