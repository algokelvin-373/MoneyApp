package com.kelvinht.moneylife.data.repository

import com.kelvinht.moneylife.data.local.dao.SavingsDao
import com.kelvinht.moneylife.data.local.entity.AppStateEntity
import com.kelvinht.moneylife.domain.model.DailyBudget
import com.kelvinht.moneylife.domain.repository.DailyLimitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DailyLimitRepositoryImpl(
    private val dao: SavingsDao,
) : DailyLimitRepository {
    override fun observeDailyLimit(): Flow<Long> = dao.observeState(KEY).map { it.toDailyLimit() }

    override suspend fun getDailyLimit(): Long = dao.getState(KEY).toDailyLimit()

    override suspend fun setDailyLimit(limit: Long) {
        require(DailyBudget.isSupportedLimit(limit)) { "Unsupported daily limit: $limit" }
        dao.saveState(AppStateEntity(stateKey = KEY, value = limit.toString()))
    }

    private fun String?.toDailyLimit(): Long =
        this?.toLongOrNull()?.takeIf(DailyBudget::isSupportedLimit) ?: DailyBudget.DEFAULT_DAILY_LIMIT

    private companion object {
        const val KEY = "daily_limit"
    }
}
