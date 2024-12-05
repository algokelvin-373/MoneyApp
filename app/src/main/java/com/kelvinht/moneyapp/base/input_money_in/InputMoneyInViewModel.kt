package com.kelvinht.moneyapp.base.input_money_in

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.kelvinht.moneyapp.data.Transaction
import com.kelvinht.moneyapp.repository.AddMoneyInRepository

class InputMoneyInViewModel(
    context: Context,
    date: String,
): ViewModel() {
    private val repository = AddMoneyInRepository(context, date)

    fun insert(transaction: Transaction) = liveData {
        val statusInsert = repository.insert(transaction)
        emit(statusInsert)
    }
}