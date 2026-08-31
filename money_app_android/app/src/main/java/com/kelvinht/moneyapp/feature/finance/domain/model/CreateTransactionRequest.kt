package com.kelvinht.moneyapp.feature.finance.domain.model

data class CreateTransactionRequest(
    val account_id: String,
    val code: String,
    val transaction_type: String,
    val amount: Long,
    val transaction_date: String,
    val note: String,
    val idempotency_key: String
)
