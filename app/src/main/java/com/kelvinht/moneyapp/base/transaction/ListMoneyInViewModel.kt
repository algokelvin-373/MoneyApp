package com.kelvinht.moneyapp.base.transaction

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.kelvinht.moneyapp.repository.ListMoneyInRepository

class ListMoneyInViewModel(context: Context) : ViewModel() {
    private val repository = ListMoneyInRepository(context)

    fun getAllTransaction() = liveData {
        val allTransaction = repository.getAllMoneyIn()
        emit(allTransaction)
    }
}
