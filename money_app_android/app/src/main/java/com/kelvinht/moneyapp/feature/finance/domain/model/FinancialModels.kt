package com.kelvinht.moneyapp.feature.finance.domain.model

data class Account(
    val id: String,
    val code: String,
    val name: String,
    val balance: Long,
    val is_active: Boolean
)

data class TransactionCode(
    val id: String,
    val code: String,
    val name: String,
    val type: String,
    val is_active: Boolean
)

data class FinanceTransaction(
    val id: String,
    val account_id: String,
    val account_code: String,
    val code: String,
    val transaction_type: String,
    val amount: Long,
    val transaction_date: String,
    val note: String
)

data class DashboardSummary(
    val total_balance: Long,
    val total_income: Long,
    val total_expense: Long,
    val cash_flow: Long,
    val accounts: List<Account>,
    val recent_transactions: List<FinanceTransaction>
)

data class SpendingCategory(
    val code: String,
    val amount: Long,
    val percentage: Double
)

data class SpendingSummary(
    val total_expense: Long,
    val categories: List<SpendingCategory>
)
