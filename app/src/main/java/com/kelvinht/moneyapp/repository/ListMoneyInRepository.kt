package com.kelvinht.moneyapp.repository

import android.content.Context
import androidx.room.Room.databaseBuilder
import com.kelvinht.moneyapp.data.Transaction
import com.kelvinht.moneyapp.db.AppDatabase
import com.kelvinht.moneyapp.db.TransactionDao

class ListMoneyInRepository(context: Context) {
    private val transactionDao: TransactionDao?
    //private val transactions: List<Transaction>

    init {
        val db = databaseBuilder(context, AppDatabase::class.java, "transaction_db").build()
        transactionDao = db.transactionDao()
        //transactions = transactionDao?.getTransactionsByDate()!!
    }

    suspend fun getAllMoneyIn(): List<Transaction> {
        return transactionDao?.getTransactionsByDate()!!
    }
}