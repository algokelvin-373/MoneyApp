package com.kelvinht.moneyapp.base.list_money_in

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.kelvinht.moneyapp.data.MoneyIn
import com.kelvinht.moneyapp.repository.ListMoneyInRepository

class ListMoneyInViewModel(context: Context) : ViewModel() {
    private val repository = ListMoneyInRepository(context)

    fun getAllTransaction() = liveData {
        val allTransaction = repository.getAllMoneyIn()
        emit(allTransaction)
    }

    fun deleteMoneyIn(moneyIn: MoneyIn) = liveData {
        val statusDelete = repository.deleteMoneyIn(moneyIn)
        emit(statusDelete)
    }
}
