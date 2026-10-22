package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.domain.repository.ExpenseRepository
import com.kelvinht.moneylife.domain.repository.SavingsRepository

class RolloverDailySavingsUseCase(
    private val expenseRepository: ExpenseRepository,
    private val savingsRepository: SavingsRepository
) {
    suspend operator fun invoke(todayKey: String, dailyLimit: Long) {
        val lastActiveDate = savingsRepository.getLastActiveDate()

        if (lastActiveDate == null) {
            savingsRepository.setLastActiveDate(todayKey)
            return
        }

        if (lastActiveDate >= todayKey) return

        val spent = expenseRepository.getExpensesByDate(lastActiveDate).sumOf { it.amount }
        val saving = (dailyLimit - spent).coerceAtLeast(0L)

        savingsRepository.saveDailySaving(lastActiveDate, saving)
        savingsRepository.setLastActiveDate(todayKey)
    }
}
