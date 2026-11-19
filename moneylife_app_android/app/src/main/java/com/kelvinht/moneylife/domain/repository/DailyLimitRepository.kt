package com.kelvinht.moneylife.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Stores the daily limit chosen by the user and exposes it reactively so the
 * home screen, the savings rollover and the expense use cases always work with
 * the same limit.
 */
interface DailyLimitRepository {
    fun observeDailyLimit(): Flow<Long>

    suspend fun getDailyLimit(): Long

    suspend fun setDailyLimit(limit: Long)
}

/** Supplies the currently selected daily limit to the expense use cases. */
fun interface DailyLimitProvider {
    suspend fun current(): Long
}
