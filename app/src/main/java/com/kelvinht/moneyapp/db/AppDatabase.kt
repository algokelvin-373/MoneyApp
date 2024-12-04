package com.kelvinht.moneyapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kelvinht.moneyapp.data.Transaction


@Database(entities = [Transaction::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao?
}