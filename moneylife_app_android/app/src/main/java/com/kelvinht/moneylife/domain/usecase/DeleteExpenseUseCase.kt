package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.repository.ExpenseRepository
import com.kelvinht.moneylife.domain.repository.SavingsRepository
import com.kelvinht.moneylife.domain.repository.TransactionRunner

/**
 * Deletes a single expense by its id.
 *
 * The deletion and the recalculated daily saving are written inside a single
 * transaction, so the savings balance can never get out of sync with the
 * recorded expenses.
 */
class DeleteExpenseUseCase(
    private val repository: ExpenseRepository,
    private val savingsRepository: SavingsRepository,
    private val transactionRunner: TransactionRunner,
    private val dailyLimit: Long
) {
    suspend operator fun invoke(id: Long) {
        transactionRunner.runInTransaction {
            val expense = repository.getExpenseById(id)
            if (expense == null) {
                return@runInTransaction
            }
            repository.deleteExpenseById(id)
            if (expense.dateKey < DateUtils.todayKey()) {
                val spent = repository.getExpensesByDate(expense.dateKey).sumOf { it.amount }
                savingsRepository.saveDailySaving(expense.dateKey, (dailyLimit - spent).coerceAtLeast(0L))
            }
        }
    }
}
