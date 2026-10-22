package com.kelvinht.moneylife.data.mapper

import com.kelvinht.moneylife.data.local.entity.ExpenseEntity
import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.model.ExpenseCategory

/**
 * Translates between the Room entity and the domain model so the rest of the
 * app never touches persistence types.
 */
fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    category = category.toExpenseCategory(),
    amount = amount,
    note = note,
    dateKey = dateKey,
    createdAt = createdAt
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    category = category.name,
    amount = amount,
    note = note,
    dateKey = dateKey,
    createdAt = createdAt
)

private fun String.toExpenseCategory(): ExpenseCategory =
    runCatching { ExpenseCategory.valueOf(this) }.getOrDefault(ExpenseCategory.MAKAN)
