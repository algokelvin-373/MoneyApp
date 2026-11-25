package com.kelvinht.moneylife.data.repository

import com.kelvinht.moneylife.data.local.dao.SavingsDao
import com.kelvinht.moneylife.data.local.entity.AppStateEntity
import com.kelvinht.moneylife.data.local.entity.DailySavingEntity
import com.kelvinht.moneylife.domain.repository.SavingsRepository
import kotlinx.coroutines.flow.Flow

class SavingsRepositoryImpl(
    private val dao: SavingsDao,
) : SavingsRepository {
    override fun observeBalance(monthKey: String): Flow<Long> = dao.observeBalance(monthKey)

    override suspend fun getMonthlySavings(monthKey: String): Long =
        dao.getMonthlySavings(monthKey)

    override suspend fun saveDailySaving(
        dateKey: String,
        amount: Long,
    ) {
        dao.saveDailySaving(DailySavingEntity(dateKey = dateKey, amount = amount))
    }

    override suspend fun saveDailySavings(savings: Map<String, Long>) {
        if (savings.isEmpty()) return
        dao.saveDailySavings(
            savings.map { (dateKey, amount) ->
                DailySavingEntity(
                    dateKey,
                    amount,
                )
            },
        )
    }

    override suspend fun getLastActiveDate(): String? = dao.getState(LAST_ACTIVE_DATE)

    override suspend fun setLastActiveDate(dateKey: String) {
        dao.saveState(AppStateEntity(stateKey = LAST_ACTIVE_DATE, value = dateKey))
    }

    private companion object {
        const val LAST_ACTIVE_DATE = "last_active_date"
    }
}
