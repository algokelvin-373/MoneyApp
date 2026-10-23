package com.kelvinht.moneylife.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_savings")
data class DailySavingEntity(
    @PrimaryKey val dateKey: String,
    val amount: Long,
)
