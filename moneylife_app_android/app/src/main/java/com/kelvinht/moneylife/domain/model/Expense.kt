package com.kelvinht.moneylife.domain.model

/**
 * A single expense record. [dateKey] is a "yyyy-MM-dd" string used for
 * grouping and querying expenses per day.
 */
data class Expense(
    val id: Long = 0L,
    val category: ExpenseCategory,
    val amount: Long,
    val note: String = "",
    val dateKey: String,
    val createdAt: Long,
)
