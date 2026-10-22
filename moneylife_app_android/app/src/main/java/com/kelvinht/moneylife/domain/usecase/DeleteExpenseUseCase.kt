package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.repository.ExpenseRepository
import com.kelvinht.moneylife.domain.repository.SavingsRepository

/**
 * Deletes a single expense by its id.
 */
class DeleteExpenseUseCase(
    private val repository: ExpenseRepository,
    private val savingsRepository: SavingsRepository,
    private val dailyLimit: Long
) {
    suspend operator fun invoke(id: Long) {
        val expense = repository.getExpenseById(id) ?: return
        repository.deleteExpenseById(id)
        if (expense.dateKey < DateUtils.todayKey()) {
            val spent = repository.getExpensesByDate(expense.dateKey).sumOf { it.amount }
            savingsRepository.saveDailySaving(expense.dateKey, (dailyLimit - spent).coerceAtLeast(0L))
        }
    }
}
