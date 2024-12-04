package com.kelvinht.moneyapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions_data")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo("data_into")
    val dataInto: String,

    @ColumnInfo("data_from")
    val dataFrom: String,

    @ColumnInfo("description")
    val description: String,

    @ColumnInfo("type")
    val type: String,

    @ColumnInfo("photo")
    val photoPath: String,

    @ColumnInfo("time")
    val time: String,

    @ColumnInfo("date")
    var date: String,

    @ColumnInfo("amount")
    var amount: Int,
)
