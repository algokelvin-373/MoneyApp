package com.kelvinht.moneyapp.repository

import android.app.Application
import android.content.Context
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.room.Room.databaseBuilder
import com.kelvinht.moneyapp.data.Transaction
import com.kelvinht.moneyapp.db.AppDatabase
import com.kelvinht.moneyapp.db.TransactionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class TransactionRepository(context: Context, date: String?) {
    private val transactionDao: TransactionDao?
    private val transactions: LiveData<List<Transaction>>

    init {
        val db = databaseBuilder(context, AppDatabase::class.java, "transaction_db").build()
        transactionDao = db.transactionDao()
        transactions = transactionDao?.getTransactionsByDate(date)!!
    }

    fun getTransactions(): LiveData<List<Transaction>> = transactions

    suspend fun insert(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            transactionDao?.insertTransaction(transaction)
        }
        //InsertTransactionAsyncTask(transactionDao).execute(transaction)
    }

    /*private class InsertTransactionAsyncTask(private val transactionDao: TransactionDao?) :
        AsyncTask<Transaction?, Void?, Void?>() {
        override fun doInBackground(vararg transactions: Transaction): Void? {
            transactionDao!!.insertTransaction(transactions[0])
            return null
        }
    }*/
}
