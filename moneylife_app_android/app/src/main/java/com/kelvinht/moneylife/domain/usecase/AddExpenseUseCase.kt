package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.repository.ExpenseRepository

/**
 * Validates and persists a new expense.
 */
class AddExpenseUseCase(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(
        category: ExpenseCategory,
        amount: Long,
        note: String,
        dateKey: String = DateUtils.todayKey()
    ): Result<Long> {
        if (amount <= 0L) {
            return Result.failure(IllegalArgumentException("Nominal harus lebih dari 0"))
        }

        val expense = Expense(
            category = category,
            amount = amount,
            note = note.trim(),
            dateKey = dateKey,
            createdAt = System.currentTimeMillis()
        )

        return runCatching { repository.addExpense(expense) }
    }
}
