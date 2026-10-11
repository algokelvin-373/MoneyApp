package com.kelvinht.moneyapp.feature.transactionentry.presentation

data class TransactionEntryUiState(
    val amount: String,
    val categories: List<EntryCategoryUi>,
    val transactionDate: String,
    val source: String,
    val note: String,
    val isSaveEnabled: Boolean
)

data class EntryCategoryUi(val name: String, val isSelected: Boolean)
