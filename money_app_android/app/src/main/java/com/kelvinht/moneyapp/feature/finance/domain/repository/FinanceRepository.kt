package com.kelvinht.moneyapp.feature.finance.domain.repository

import com.kelvinht.moneyapp.feature.finance.domain.model.Account
import com.kelvinht.moneyapp.feature.finance.domain.model.CreateTransactionRequest
import com.kelvinht.moneyapp.feature.finance.domain.model.DashboardSummary
import com.kelvinht.moneyapp.feature.finance.domain.model.FinanceTransaction
import com.kelvinht.moneyapp.feature.finance.domain.model.SpendingSummary
import com.kelvinht.moneyapp.feature.finance.domain.model.TransactionCode

interface FinanceRepository {
    suspend fun getDashboardSummary(token: String): DashboardSummary
    suspend fun getAccounts(token: String): List<Account>
    suspend fun getTransactionCodes(token: String, type: String? = null): List<TransactionCode>
    suspend fun getTransactions(
        token: String,
        account: String? = null,
        type: String? = null,
        code: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): List<FinanceTransaction>
    suspend fun createTransaction(token: String, request: CreateTransactionRequest): FinanceTransaction
    suspend fun getSpendingSummary(token: String, startDate: String? = null, endDate: String? = null): SpendingSummary
}
