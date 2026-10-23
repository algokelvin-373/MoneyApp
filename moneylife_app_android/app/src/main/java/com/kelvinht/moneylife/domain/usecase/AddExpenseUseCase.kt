package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.repository.ExpenseRepository
import com.kelvinht.moneylife.domain.repository.SavingsRepository
import com.kelvinht.moneylife.domain.repository.TransactionRunner

/**
 * Validates and persists a new expense.
 *
 * The expense and the matching daily saving are written inside a single
 * transaction, so the savings balance can never get out of sync with the
 * recorded expenses.
 */
class AddExpenseUseCase(
    private val repository: ExpenseRepository,
    private val savingsRepository: SavingsRepository,
    private val transactionRunner: TransactionRunner,
    private val dailyLimit: Long
) {
    suspend operator fun invoke(
        category: ExpenseCategory,
        amount: Long,
        note: String,
        dateKey: String = DateUtils.todayKey()
    ): Result<Long> {
        if (amount <= 0L) {
            return Result.failure(IllegalArgumentException())
        }

        val expense = Expense(
            category = category,
            amount = amount,
            note = note.trim(),
            dateKey = dateKey,
            createdAt = System.currentTimeMillis()
        )

        return runCatching {
            transactionRunner.runInTransaction {
                val id = repository.addExpense(expense)
                if (DateUtils.isPast(dateKey)) {
                    val spent = repository.getExpensesByDate(dateKey).sumOf { it.amount }
                    savingsRepository.saveDailySaving(dateKey, (dailyLimit - spent).coerceAtLeast(0L))
                }
                id
            }
        }
    }
}
