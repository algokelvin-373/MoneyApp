package com.kelvinht.moneylife.domain

import com.kelvinht.moneylife.domain.repository.SavingsRepository
import com.kelvinht.moneylife.domain.usecase.ObserveSavingsBalanceUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveSavingsBalanceUseCaseTest {
    @Test
    fun balanceOnlyCountsSavingsInRequestedMonth() =
        runBlocking {
            val repository =
                MonthSavingsRepository(
                    saved =
                        mapOf(
                            "2026-09-30" to 20_000L,
                            "2026-10-01" to 50_000L,
                            "2026-10-02" to 30_000L,
                        ),
                )
            val useCase = ObserveSavingsBalanceUseCase(repository)

            assertEquals(80_000L, useCase("2026-10").first())
        }

    @Test
    fun balanceIsZeroWhenNoSavingsInMonth() =
        runBlocking {
            val repository = MonthSavingsRepository(saved = mapOf("2026-09-30" to 20_000L))
            val useCase = ObserveSavingsBalanceUseCase(repository)

            assertEquals(0L, useCase("2026-10").first())
        }

    @Test
    fun previousMonthStillReachable() =
        runBlocking {
            val repository =
                MonthSavingsRepository(
                    saved =
                        mapOf(
                            "2026-09-29" to 10_000L,
                            "2026-09-30" to 20_000L,
                        ),
                )
            val useCase = ObserveSavingsBalanceUseCase(repository)

            assertEquals(30_000L, useCase("2026-09").first())
        }
}

private class MonthSavingsRepository(
    private val saved: Map<String, Long> = emptyMap(),
) : SavingsRepository {
    override fun observeBalance(monthKey: String): Flow<Long> = MutableStateFlow(saved.filterKeys { it.startsWith(monthKey) }.values.sum())

    override suspend fun saveDailySaving(
        dateKey: String,
        amount: Long,
    ) = Unit

    override suspend fun saveDailySavings(savings: Map<String, Long>) = Unit

    override suspend fun getLastActiveDate(): String? = null

    override suspend fun setLastActiveDate(dateKey: String) = Unit
}
