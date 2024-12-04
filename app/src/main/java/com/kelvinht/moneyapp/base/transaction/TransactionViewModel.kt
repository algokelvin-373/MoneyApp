package com.kelvinht.moneyapp.base.transaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.kelvinht.moneyapp.data.Transaction
import com.kelvinht.moneyapp.repository.TransactionRepository

class TransactionViewModel(application: Application, date: String?) :
    AndroidViewModel(application) {
    private val repository: TransactionRepository
    private val transactions: LiveData<List<Transaction>>

    init {
        repository = TransactionRepository(application, date)
        transactions = repository.getTransactions()
    }

    fun getTransactions(): LiveData<List<Transaction>> {
        return transactions
    }

    fun insert(transaction: Transaction?) {
        //repository.insert(transaction)
    }
}
