package com.kelvinht.moneylife.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_state")
data class AppStateEntity(
    @PrimaryKey val stateKey: String,
    val value: String,
)
