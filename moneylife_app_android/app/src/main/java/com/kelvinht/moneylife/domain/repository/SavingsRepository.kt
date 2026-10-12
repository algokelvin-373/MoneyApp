package com.kelvinht.moneylife.domain.repository

import kotlinx.coroutines.flow.Flow

interface SavingsRepository {
    fun observeBalance(): Flow<Long>

    suspend fun saveDailySaving(dateKey: String, amount: Long)

    suspend fun getLastActiveDate(): String?

    suspend fun setLastActiveDate(dateKey: String)
}
