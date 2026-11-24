package com.kelvinht.moneylife.data.repository

import com.kelvinht.moneylife.data.local.dao.ExpenseDao
import com.kelvinht.moneylife.data.mapper.toDomain
import com.kelvinht.moneylife.data.mapper.toEntity
import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepositoryImpl(
    private val dao: ExpenseDao,
) : ExpenseRepository {
    override fun observeExpensesByDate(dateKey: String): Flow<List<Expense>> =
        dao.observeByDate(dateKey).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getExpensesByDate(dateKey: String): List<Expense> = dao.getByDate(dateKey).map { it.toDomain() }

    override suspend fun getExpenseById(id: Long): Expense? = dao.getById(id)?.toDomain()

    override suspend fun addExpense(expense: Expense): Long = dao.insert(expense.toEntity())

    override suspend fun updateExpense(expense: Expense): Boolean = dao.update(expense.toEntity()) > 0

    override suspend fun deleteExpenseById(id: Long) = dao.deleteById(id)
}
