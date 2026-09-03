package com.kelvinht.moneyapp.feature.history.domain.model

data class TransactionEntryOptions(
    val categories: List<String>,
    val fundingSources: List<String>,
    val selectedCategory: String,
    val selectedSource: String,
    val datePlaceholder: String
)
