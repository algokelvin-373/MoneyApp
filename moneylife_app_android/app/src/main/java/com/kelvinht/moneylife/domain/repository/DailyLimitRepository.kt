package com.kelvinht.moneylife.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Stores the daily limit chosen by the user. Every date gets a snapshot of the
 * limit that was active on that day, so changing the limit today never rewrites
 * the budget of days that have already passed.
 */
interface DailyLimitRepository {
    fun observeDailyLimit(dateKey: String): Flow<Long>

    suspend fun getDailyLimit(dateKey: String): Long

    suspend fun setDailyLimit(
        dateKey: String,
        limit: Long,
    )

    /** Locks in the current default for [dateKey] if it has no snapshot yet. */
    suspend fun snapshotDailyLimit(dateKey: String)
}

/** Supplies the daily limit of a specific date to the expense use cases. */
fun interface DailyLimitProvider {
    suspend fun forDate(dateKey: String): Long
}
