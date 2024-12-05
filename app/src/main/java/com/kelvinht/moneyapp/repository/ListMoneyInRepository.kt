package com.kelvinht.moneyapp.repository

import android.content.Context
import androidx.room.Room.databaseBuilder
import com.kelvinht.moneyapp.data.MoneyIn
import com.kelvinht.moneyapp.db.AppDatabase
import com.kelvinht.moneyapp.db.TransactionDao

class ListMoneyInRepository(context: Context) {
    private val transactionDao: TransactionDao

    init {
        val db = databaseBuilder(context, AppDatabase::class.java, "transaction_db").build()
        transactionDao = db.transactionDao()
    }

    suspend fun getAllMoneyIn(): List<MoneyIn> {
        return transactionDao.getTransactionsByDate()
    }
}