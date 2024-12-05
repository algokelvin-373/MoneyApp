package com.kelvinht.moneyapp.repository

import android.content.Context
import androidx.room.Room.databaseBuilder
import com.kelvinht.moneyapp.data.MoneyIn
import com.kelvinht.moneyapp.db.AppDatabase
import com.kelvinht.moneyapp.db.TransactionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class AddMoneyInRepository(context: Context, date: String?) {
    private val transactionDao: TransactionDao

    init {
        val db = databaseBuilder(context, AppDatabase::class.java, "transaction_db").build()
        transactionDao = db.transactionDao()
    }

    suspend fun insert(moneyIn: MoneyIn) {
        withContext(Dispatchers.IO) {
            transactionDao.insertTransaction(moneyIn)
        }
    }
}
