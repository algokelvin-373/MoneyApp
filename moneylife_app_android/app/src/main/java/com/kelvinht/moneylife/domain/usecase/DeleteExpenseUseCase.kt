package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.domain.repository.ExpenseRepository

/**
 * Deletes a single expense by its id.
 */
class DeleteExpenseUseCase(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteExpenseById(id)
}
