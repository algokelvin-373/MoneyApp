package com.kelvinht.moneyapp.feature.transactionentry.presentation

data class TransactionEntryUiState(
    val amount: String,
    val category: String,
    val datePlaceholder: String,
    val sources: List<FundingSourceUi>,
    val isSaveEnabled: Boolean
)

data class FundingSourceUi(val name: String, val isSelected: Boolean)
