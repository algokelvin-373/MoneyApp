package com.kelvinht.moneylife.domain

import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.repository.ExpenseRepository
import com.kelvinht.moneylife.domain.repository.SavingsRepository
import com.kelvinht.moneylife.domain.usecase.RolloverDailySavingsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RolloverDailySavingsUseCaseTest {
    @Test
    fun firstOpen_onlyRegistersActiveDate() =
        runBlocking {
            val savings = FakeSavingsRepository()
            val useCase = RolloverDailySavingsUseCase(FakeExpenseRepository(), savings, FakeTransactionRunner())

            useCase("2026-10-12", DAILY_LIMIT)

            assertEquals("2026-10-12", savings.activeDate)
            assertTrue(savings.saved.isEmpty())
        }

    @Test
    fun newDay_savesRemainingBudget() =
        runBlocking {
            val expenses = FakeExpenseRepository(mapOf("2026-10-11" to listOf(expense(60_000, "2026-10-11"))))
            val savings = FakeSavingsRepository(initialDate = "2026-10-11")
            val useCase = RolloverDailySavingsUseCase(expenses, savings, FakeTransactionRunner())

            useCase("2026-10-12", DAILY_LIMIT)

            assertEquals(40_000L, savings.saved["2026-10-11"])
            assertEquals("2026-10-12", savings.activeDate)
        }

    @Test
    fun overLimit_savesZero() =
        runBlocking {
            val expenses = FakeExpenseRepository(mapOf("2026-10-11" to listOf(expense(120_000, "2026-10-11"))))
            val savings = FakeSavingsRepository(initialDate = "2026-10-11")
            val useCase = RolloverDailySavingsUseCase(expenses, savings, FakeTransactionRunner())

            useCase("2026-10-12", DAILY_LIMIT)

            assertEquals(0L, savings.saved["2026-10-11"])
        }

    @Test
    fun spentExactlyAtLimit_savesZero() =
        runBlocking {
            val expenses = FakeExpenseRepository(mapOf("2026-10-11" to listOf(expense(DAILY_LIMIT, "2026-10-11"))))
            val savings = FakeSavingsRepository(initialDate = "2026-10-11")
            val useCase = RolloverDailySavingsUseCase(expenses, savings, FakeTransactionRunner())

            useCase("2026-10-12", DAILY_LIMIT)

            assertEquals(0L, savings.saved["2026-10-11"])
        }

    @Test
    fun unusedDay_savesFullLimit() =
        runBlocking {
            val expenses = FakeExpenseRepository(emptyMap())
            val savings = FakeSavingsRepository(initialDate = "2026-10-11")
            val useCase = RolloverDailySavingsUseCase(expenses, savings, FakeTransactionRunner())

            useCase("2026-10-12", DAILY_LIMIT)

            assertEquals(100_000L, savings.saved["2026-10-11"])
        }

    @Test
    fun sameDay_doesNotCalculateTwice() =
        runBlocking {
            val savings = FakeSavingsRepository(initialDate = "2026-10-12")
            val useCase = RolloverDailySavingsUseCase(FakeExpenseRepository(), savings, FakeTransactionRunner())

            useCase("2026-10-12", DAILY_LIMIT)

            assertTrue(savings.saved.isEmpty())
        }

    @Test
    fun missedDays_areSettledOneByOne() =
        runBlocking {
            // App was last opened on the 20th and is opened again on the 23rd:
            // the 20th, 21st and 22nd all have to be settled.
            val expenses =
                FakeExpenseRepository(
                    mapOf(
                        "2026-10-20" to listOf(expense(30_000, "2026-10-20")),
                        "2026-10-22" to listOf(expense(120_000, "2026-10-22")),
                    ),
                )
            val savings = FakeSavingsRepository(initialDate = "2026-10-20")
            val useCase = RolloverDailySavingsUseCase(expenses, savings, FakeTransactionRunner())

            useCase("2026-10-23", DAILY_LIMIT)

            assertEquals(70_000L, savings.saved["2026-10-20"])
            assertEquals(100_000L, savings.saved["2026-10-21"])
            assertEquals(0L, savings.saved["2026-10-22"])
            assertEquals("2026-10-23", savings.activeDate)
        }

    @Test
    fun longGap_settlesEveryMissedDay() =
        runBlocking {
            val savings = FakeSavingsRepository(initialDate = "2026-10-01")
            val useCase = RolloverDailySavingsUseCase(FakeExpenseRepository(), savings, FakeTransactionRunner())

            useCase("2026-10-08", DAILY_LIMIT)

            assertEquals(7, savings.saved.size)
            assertTrue(savings.saved.keys.containsAll(listOf("2026-10-01", "2026-10-02", "2026-10-03", "2026-10-04")))
            assertTrue(savings.saved.keys.containsAll(listOf("2026-10-05", "2026-10-06", "2026-10-07")))
            assertFalse(savings.saved.containsKey("2026-10-08"))
        }

    @Test
    fun today_isNotSettledBecauseItIsNotFinalYet() =
        runBlocking {
            val expenses = FakeExpenseRepository(mapOf("2026-10-23" to listOf(expense(10_000, "2026-10-23"))))
            val savings = FakeSavingsRepository(initialDate = "2026-10-22")
            val useCase = RolloverDailySavingsUseCase(expenses, savings, FakeTransactionRunner())

            useCase("2026-10-23", DAILY_LIMIT)

            assertFalse(savings.saved.containsKey("2026-10-23"))
            assertEquals(100_000L, savings.saved["2026-10-22"])
        }

    @Test
    fun allSettlementsAreWrittenInASingleTransaction() =
        runBlocking {
            val transactionRunner = FakeTransactionRunner()
            val savings = FakeSavingsRepository(initialDate = "2026-10-20")
            val useCase = RolloverDailySavingsUseCase(FakeExpenseRepository(), savings, transactionRunner)

            useCase("2026-10-23", DAILY_LIMIT)

            assertEquals(1, transactionRunner.transactions)
        }

    private fun expense(
        amount: Long,
        dateKey: String,
    ) = Expense(
        category = ExpenseCategory.MAKAN,
        amount = amount,
        dateKey = dateKey,
        createdAt = 0L,
    )

    private companion object {
        const val DAILY_LIMIT = 100_000L
    }
}

private class FakeExpenseRepository(
    private val expensesByDate: Map<String, List<Expense>> = emptyMap(),
) : ExpenseRepository {
    override fun observeExpensesByDate(dateKey: String): Flow<List<Expense>> = emptyFlow()

    override suspend fun getExpensesByDate(dateKey: String): List<Expense> = expensesByDate[dateKey].orEmpty()

    override suspend fun getExpenseById(id: Long): Expense? = null

    override suspend fun addExpense(expense: Expense): Long = 0L

    override suspend fun deleteExpenseById(id: Long) = Unit
}

private class FakeSavingsRepository(
    initialDate: String? = null,
) : SavingsRepository {
    var activeDate: String? = initialDate
    val saved = linkedMapOf<String, Long>()
    private val balance = MutableStateFlow(0L)

    override fun observeBalance(monthKey: String): Flow<Long> = balance

    override suspend fun saveDailySaving(
        dateKey: String,
        amount: Long,
    ) {
        saved[dateKey] = amount
        balance.value = saved.values.sum()
    }

    override suspend fun saveDailySavings(savings: Map<String, Long>) {
        saved.putAll(savings)
        balance.value = saved.values.sum()
    }

    override suspend fun getLastActiveDate(): String? = activeDate

    override suspend fun setLastActiveDate(dateKey: String) {
        activeDate = dateKey
    }
}
