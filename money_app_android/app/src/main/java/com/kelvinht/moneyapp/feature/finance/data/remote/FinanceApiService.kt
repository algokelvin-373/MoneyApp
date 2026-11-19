package com.kelvinht.moneyapp.feature.finance.data.remote

import com.kelvinht.moneyapp.core.network.ApiResponse
import com.kelvinht.moneyapp.feature.finance.domain.model.Account
import com.kelvinht.moneyapp.feature.finance.domain.model.CreateTransactionRequest
import com.kelvinht.moneyapp.feature.finance.domain.model.DashboardSummary
import com.kelvinht.moneyapp.feature.finance.domain.model.FinanceTransaction
import com.kelvinht.moneyapp.feature.finance.domain.model.SpendingSummary
import com.kelvinht.moneyapp.feature.finance.domain.model.TransactionCode
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface FinanceApiService {
    @GET("dashboard/summary")
    suspend fun getDashboardSummary(@Header("Authorization") token: String): ApiResponse<DashboardSummary>

    @GET("accounts")
    suspend fun getAccounts(@Header("Authorization") token: String): ApiResponse<List<Account>>

    @GET("transaction-codes")
    suspend fun getTransactionCodes(
        @Header("Authorization") token: String,
        @Query("type") type: String?
    ): ApiResponse<List<TransactionCode>>

    @GET("transactions")
    suspend fun getTransactions(
        @Header("Authorization") token: String,
        @Query("account") account: String?,
        @Query("type") type: String?,
        @Query("code") code: String?,
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?
    ): ApiResponse<List<FinanceTransaction>>

    @POST("transactions")
    suspend fun createTransaction(
        @Header("Authorization") token: String,
        @Body request: CreateTransactionRequest
    ): ApiResponse<FinanceTransaction>

    @GET("insights/spending")
    suspend fun getSpendingSummary(
        @Header("Authorization") token: String,
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?
    ): ApiResponse<SpendingSummary>
}
