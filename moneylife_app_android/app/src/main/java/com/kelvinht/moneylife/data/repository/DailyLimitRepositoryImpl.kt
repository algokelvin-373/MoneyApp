package com.kelvinht.moneylife.data.repository

import com.kelvinht.moneylife.data.local.dao.SavingsDao
import com.kelvinht.moneylife.data.local.entity.AppStateEntity
import com.kelvinht.moneylife.domain.model.DailyBudget
import com.kelvinht.moneylife.domain.repository.DailyLimitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DailyLimitRepositoryImpl(
    private val dao: SavingsDao,
) : DailyLimitRepository {
    override fun observeDailyLimit(dateKey: String): Flow<Long> =
        combine(
            dao.observeState(dateLimitKey(dateKey)),
            dao.observeState(DEFAULT_KEY),
        ) { dateLimit, defaultLimit ->
            dateLimit.toDailyLimitOrNull()
                ?: defaultLimit.toDailyLimitOrNull()
                ?: DailyBudget.DEFAULT_DAILY_LIMIT
        }

    override suspend fun getDailyLimit(dateKey: String): Long =
        dao.getState(dateLimitKey(dateKey)).toDailyLimitOrNull()
            ?: dao.getState(DEFAULT_KEY).toDailyLimitOrNull()
            ?: DailyBudget.DEFAULT_DAILY_LIMIT

    override suspend fun setDailyLimit(
        dateKey: String,
        limit: Long,
    ) {
        require(DailyBudget.isSupportedLimit(limit)) { "Unsupported daily limit: $limit" }
        dao.saveState(AppStateEntity(stateKey = DEFAULT_KEY, value = limit.toString()))
        dao.saveState(AppStateEntity(stateKey = dateLimitKey(dateKey), value = limit.toString()))
    }

    override suspend fun snapshotDailyLimit(dateKey: String) {
        val limit =
            dao.getState(DEFAULT_KEY).toDailyLimitOrNull()
                ?: DailyBudget.DEFAULT_DAILY_LIMIT
        dao.saveStateIfAbsent(
            AppStateEntity(stateKey = dateLimitKey(dateKey), value = limit.toString()),
        )
    }

    private fun String?.toDailyLimitOrNull(): Long? = this?.toLongOrNull()?.takeIf(DailyBudget::isSupportedLimit)

    private fun dateLimitKey(dateKey: String): String = "$DATE_LIMIT_PREFIX$dateKey"

    private companion object {
        const val DEFAULT_KEY = "daily_limit"
        const val DATE_LIMIT_PREFIX = "daily_limit_"
    }
}
