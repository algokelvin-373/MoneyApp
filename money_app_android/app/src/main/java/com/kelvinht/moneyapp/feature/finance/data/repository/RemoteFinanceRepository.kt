package com.kelvinht.moneyapp.feature.finance.data.repository

import com.kelvinht.moneyapp.core.network.ApiResponse
import com.kelvinht.moneyapp.feature.finance.data.remote.FinanceApiService
import com.kelvinht.moneyapp.feature.finance.domain.model.Account
import com.kelvinht.moneyapp.feature.finance.domain.model.CreateTransactionRequest
import com.kelvinht.moneyapp.feature.finance.domain.model.DashboardSummary
import com.kelvinht.moneyapp.feature.finance.domain.model.FinanceTransaction
import com.kelvinht.moneyapp.feature.finance.domain.model.SpendingSummary
import com.kelvinht.moneyapp.feature.finance.domain.model.TransactionCode
import com.kelvinht.moneyapp.feature.finance.domain.repository.FinanceRepository

class RemoteFinanceRepository(
    private val api: FinanceApiService
) : FinanceRepository {
    override suspend fun getDashboardSummary(token: String): DashboardSummary {
        return api.getDashboardSummary(token.asBearer()).requireData()
    }

    override suspend fun getAccounts(token: String): List<Account> {
        return api.getAccounts(token.asBearer()).requireData()
    }

    override suspend fun getTransactionCodes(token: String, type: String?): List<TransactionCode> {
        return api.getTransactionCodes(token.asBearer(), type).requireData()
    }

    override suspend fun getTransactions(
        token: String,
        account: String?,
        type: String?,
        code: String?,
        startDate: String?,
        endDate: String?
    ): List<FinanceTransaction> {
        return api.getTransactions(token.asBearer(), account, type, code, startDate, endDate).requireData()
    }

    override suspend fun createTransaction(token: String, request: CreateTransactionRequest): FinanceTransaction {
        return api.createTransaction(token.asBearer(), request).requireData()
    }

    override suspend fun getSpendingSummary(token: String, startDate: String?, endDate: String?): SpendingSummary {
        return api.getSpendingSummary(token.asBearer(), startDate, endDate).requireData()
    }

    private fun String.asBearer(): String = if (startsWith("Bearer ")) this else "Bearer $this"

    private fun <T> ApiResponse<T>.requireData(): T {
        if (!success || data == null) {
            throw IllegalStateException(error_code ?: message)
        }
        return data
    }
}
