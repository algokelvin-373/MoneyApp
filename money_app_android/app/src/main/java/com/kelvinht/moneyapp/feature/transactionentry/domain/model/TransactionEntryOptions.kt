package com.kelvinht.moneyapp.feature.transactionentry.domain.model

data class TransactionEntryOptions(
    val categories: List<String>,
    val fundingSources: List<String>,
    val selectedCategory: String,
    val selectedSource: String,
    val transactionDate: String,
    val note: String,
    val initialAmount: Long
)
