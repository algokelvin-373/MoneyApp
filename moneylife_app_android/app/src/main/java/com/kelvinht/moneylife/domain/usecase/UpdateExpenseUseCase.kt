package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.repository.DailyLimitProvider
import com.kelvinht.moneylife.domain.repository.ExpenseRepository
import com.kelvinht.moneylife.domain.repository.SavingsRepository
import com.kelvinht.moneylife.domain.repository.TransactionRunner

/**
 * Updates an expense and recalculates savings for its settled day.
 */
class UpdateExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
    private val savingsRepository: SavingsRepository,
    private val transactionRunner: TransactionRunner,
    private val dailyLimitProvider: DailyLimitProvider,
) {
    suspend operator fun invoke(
        id: Long,
        category: ExpenseCategory,
        amount: Long,
        note: String,
    ): Result<Unit> {
        if (amount <= 0L) {
            return Result.failure(IllegalArgumentException("Amount must be positive"))
        }

        return runCatching {
            transactionRunner.runInTransaction {
                val existing =
                    expenseRepository.getExpenseById(id)
                        ?: throw NoSuchElementException("Expense $id was not found")
                val updated =
                    existing.copy(
                        category = category,
                        amount = amount,
                        note = note.trim(),
                    )
                if (!expenseRepository.updateExpense(updated)) {
                    throw NoSuchElementException("Expense $id was not found")
                }

                if (DateUtils.isPast(existing.dateKey)) {
                    val spent =
                        expenseRepository.getExpensesByDate(existing.dateKey).sumOf { it.amount }
                    val dailyLimit = dailyLimitProvider.forDate(existing.dateKey)
                    savingsRepository.saveDailySaving(
                        existing.dateKey,
                        (dailyLimit - spent).coerceAtLeast(0L),
                    )
                }
            }
        }
    }
}
