package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow

/**
 * Streams all expenses recorded for a given day, newest first.
 */
class GetDailyExpensesUseCase(
    private val repository: ExpenseRepository
) {
    operator fun invoke(dateKey: String): Flow<List<Expense>> =
        repository.observeExpensesByDate(dateKey)
}
