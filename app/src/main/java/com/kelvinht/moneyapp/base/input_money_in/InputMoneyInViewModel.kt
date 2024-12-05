package com.kelvinht.moneyapp.base.input_money_in

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.kelvinht.moneyapp.data.MoneyIn
import com.kelvinht.moneyapp.repository.AddMoneyInRepository

class InputMoneyInViewModel(
    context: Context,
    date: String,
): ViewModel() {
    private val repository = AddMoneyInRepository(context, date)

    fun insert(moneyIn: MoneyIn) = liveData {
        val statusInsert = repository.insert(moneyIn)
        emit(statusInsert)
    }

    fun update(moneyIn: MoneyIn) = liveData {
        val statusUpdate = repository.updateMoneyIn(moneyIn)
        emit(statusUpdate)
    }
}