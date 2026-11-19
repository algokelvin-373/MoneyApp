package com.kelvinht.moneylife.testing

import com.kelvinht.moneylife.domain.model.DailyBudget
import com.kelvinht.moneylife.domain.repository.DailyLimitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [DailyLimitRepository] used by the ViewModel tests. */
class FakeDailyLimitRepository(
    initialLimit: Long = DailyBudget.DEFAULT_DAILY_LIMIT,
) : DailyLimitRepository {
    private val limit = MutableStateFlow(initialLimit)

    override fun observeDailyLimit(): Flow<Long> = limit

    override suspend fun getDailyLimit(): Long = limit.value

    override suspend fun setDailyLimit(limit: Long) {
        this.limit.value = limit
    }
}
