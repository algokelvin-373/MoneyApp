package com.kelvinht.moneylife.domain.repository

import kotlinx.coroutines.flow.Flow

interface SavingsRepository {
    fun observeBalance(monthKey: String): Flow<Long>

    suspend fun getMonthlySavings(monthKey: String): Long

    suspend fun saveDailySaving(
        dateKey: String,
        amount: Long,
    )

    /**
     * Saves the settled savings of several days at once. Used by the rollover
     * so a single write covers every day that has already passed.
     */
    suspend fun saveDailySavings(savings: Map<String, Long>)

    suspend fun getLastActiveDate(): String?

    suspend fun setLastActiveDate(dateKey: String)
}
