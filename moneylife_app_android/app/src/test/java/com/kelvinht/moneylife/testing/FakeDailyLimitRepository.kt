package com.kelvinht.moneylife.testing

import com.kelvinht.moneylife.domain.model.DailyBudget
import com.kelvinht.moneylife.domain.repository.DailyLimitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/** In-memory [DailyLimitRepository] used by the ViewModel tests. */
class FakeDailyLimitRepository(
    initialLimit: Long = DailyBudget.DEFAULT_DAILY_LIMIT,
) : DailyLimitRepository {
    private val defaultLimit = MutableStateFlow(initialLimit)
    private val snapshots = MutableStateFlow<Map<String, Long>>(emptyMap())

    override fun observeDailyLimit(dateKey: String): Flow<Long> =
        combine(defaultLimit, snapshots) { fallback, byDate ->
            byDate[dateKey] ?: fallback
        }

    override suspend fun getDailyLimit(dateKey: String): Long = snapshots.value[dateKey] ?: defaultLimit.value

    override suspend fun setDailyLimit(
        dateKey: String,
        limit: Long,
    ) {
        defaultLimit.value = limit
        snapshots.value = snapshots.value + (dateKey to limit)
    }

    override suspend fun snapshotDailyLimit(dateKey: String) {
        if (snapshots.value.containsKey(dateKey)) return
        snapshots.value = snapshots.value + (dateKey to defaultLimit.value)
    }
}
