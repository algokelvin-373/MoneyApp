package com.kelvinht.moneylife.domain.repository

import com.kelvinht.moneylife.domain.model.Expense
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for reading and writing expenses. The domain layer has no
 * knowledge of Room; the data layer provides the implementation.
 */
interface ExpenseRepository {
    fun observeExpensesByDate(dateKey: String): Flow<List<Expense>>

    suspend fun addExpense(expense: Expense): Long

    suspend fun deleteExpenseById(id: Long)
}
