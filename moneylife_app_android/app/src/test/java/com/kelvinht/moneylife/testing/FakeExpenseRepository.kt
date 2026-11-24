package com.kelvinht.moneylife.testing

import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [ExpenseRepository] used by the ViewModel tests.
 *
 * Every change is published through a [MutableStateFlow] so observers of the
 * repository see the same reactive behaviour as the real Room implementation.
 */
class FakeExpenseRepository : ExpenseRepository {
    private val expenses = MutableStateFlow<List<Expense>>(emptyList())
    private var nextId = 1L

    /** When true every write fails, used to cover the error paths. */
    var failOnWrite: Boolean = false

    /** Replaces the stored expenses with [items]. */
    fun seed(vararg items: Expense) {
        expenses.value = items.map { it.copy(id = nextId++) }
    }

    /** Adds a single expense and returns its generated id. */
    fun add(expense: Expense): Long {
        val id = nextId++
        expenses.value = expenses.value + expense.copy(id = id)
        return id
    }

    /** All stored expenses, newest first. */
    fun current(): List<Expense> = expenses.value.sortedByDescending { it.createdAt }

    override fun observeExpensesByDate(dateKey: String): Flow<List<Expense>> =
        expenses.map { list ->
            list.filter { it.dateKey == dateKey }.sortedByDescending { it.createdAt }
        }

    override suspend fun getExpensesByDate(dateKey: String): List<Expense> = expenses.value.filter { it.dateKey == dateKey }

    override suspend fun getExpenseById(id: Long): Expense? = expenses.value.find { it.id == id }

    override suspend fun addExpense(expense: Expense): Long {
        if (failOnWrite) throw IllegalStateException("Penulisan expense gagal")
        return add(expense)
    }

    override suspend fun updateExpense(expense: Expense): Boolean {
        if (failOnWrite) throw IllegalStateException("Pembaruan expense gagal")
        if (expenses.value.none { it.id == expense.id }) return false
        expenses.value = expenses.value.map { if (it.id == expense.id) expense else it }
        return true
    }

    override suspend fun deleteExpenseById(id: Long) {
        if (failOnWrite) throw IllegalStateException("Penghapusan expense gagal")
        expenses.value = expenses.value.filterNot { it.id == id }
    }
}
