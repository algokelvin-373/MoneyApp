package com.kelvinht.moneylife.testing

import com.kelvinht.moneylife.domain.repository.SavingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [SavingsRepository] used by the ViewModel tests. The settled daily
 * savings are kept per date so the monthly balance can be derived the same way
 * the real implementation does it.
 */
class FakeSavingsRepository : SavingsRepository {
    private val dailySavings = MutableStateFlow<Map<String, Long>>(emptyMap())

    /** The last day the app was opened, used by the rollover. */
    var lastActiveDate: String? = null

    /** The settled savings per date key. */
    fun savedSavings(): Map<String, Long> = dailySavings.value

    override fun observeBalance(monthKey: String): Flow<Long> =
        dailySavings.map { savings ->
            savings.filterKeys { it.startsWith(monthKey) }.values.sum()
        }

    override suspend fun getMonthlySavings(monthKey: String): Long =
        dailySavings.value.filterKeys { it.startsWith(monthKey) }.values.sum()

    override suspend fun saveDailySaving(
        dateKey: String,
        amount: Long,
    ) {
        dailySavings.value = dailySavings.value + (dateKey to amount)
    }

    override suspend fun saveDailySavings(savings: Map<String, Long>) {
        dailySavings.value = dailySavings.value + savings
    }

    override suspend fun getLastActiveDate(): String? = lastActiveDate

    override suspend fun setLastActiveDate(dateKey: String) {
        lastActiveDate = dateKey
    }
}
