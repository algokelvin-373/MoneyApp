package com.kelvinht.moneylife.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["dateKey"])],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val category: String,
    val amount: Long,
    val note: String,
    val dateKey: String,
    val createdAt: Long,
)
