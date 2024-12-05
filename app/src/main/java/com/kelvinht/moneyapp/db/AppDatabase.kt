package com.kelvinht.moneyapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kelvinht.moneyapp.data.MoneyIn


@Database(entities = [MoneyIn::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}