package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.repository.ExpenseRepository
import com.kelvinht.moneylife.domain.repository.SavingsRepository
import com.kelvinht.moneylife.domain.repository.TransactionRunner

/**
 * Settles the daily savings of every day that has already passed.
 *
 * The app is not necessarily opened every day. When it is opened again after a
 * few days, each missed day still has to be settled, otherwise the savings
 * balance silently loses money. The current day is never settled here because
 * it is not final yet.
 */
class RolloverDailySavingsUseCase(
    private val expenseRepository: ExpenseRepository,
    private val savingsRepository: SavingsRepository,
    private val transactionRunner: TransactionRunner,
) {
    suspend operator fun invoke(
        todayKey: String,
        dailyLimit: Long,
    ) = invoke(todayKey, limitForDate = { dailyLimit }, snapshotDailyLimit = {})

    /**
     * [limitForDate] resolves the limit that was active on each settled day.
     * [snapshotDailyLimit] locks that day's limit so later changes to today's
     * limit cannot rewrite history.
     */
    suspend operator fun invoke(
        todayKey: String,
        limitForDate: suspend (String) -> Long,
        snapshotDailyLimit: suspend (String) -> Unit,
    ) {
        val lastActiveDate = savingsRepository.getLastActiveDate()

        if (lastActiveDate == null) {
            savingsRepository.setLastActiveDate(todayKey)
            return
        }

        val lastActive = DateUtils.parseDate(lastActiveDate)
        val today = DateUtils.parseDate(todayKey)
        if (lastActive.isAfter(today)) {
            // The device clock was set ahead (for example while testing) and has
            // since been corrected backwards. Re-anchor instead of blocking the
            // rollover until the stored future date is reached again.
            savingsRepository.setLastActiveDate(todayKey)
            return
        }
        if (!lastActive.isBefore(today)) return

        transactionRunner.runInTransaction {
            // Settle every day from the last active day up to today (exclusive).
            // The last active day itself was never settled either, because the
            // day that is still running is only settled once it is over.
            val savings = LinkedHashMap<String, Long>()
            var day = lastActive
            while (day.isBefore(today)) {
                val dateKey = DateUtils.dateKey(day)
                val spent = expenseRepository.getExpensesByDate(dateKey).sumOf { it.amount }
                // A day without any spending means the whole daily limit was
                // saved, a day over the limit saves nothing.
                val dailyLimit = limitForDate(dateKey)
                savings[dateKey] = (dailyLimit - spent).coerceAtLeast(0L)
                snapshotDailyLimit(dateKey)
                day = day.plusDays(1)
            }

            savingsRepository.saveDailySavings(savings)
            savingsRepository.setLastActiveDate(todayKey)
        }
    }
}
