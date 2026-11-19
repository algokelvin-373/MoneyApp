package com.kelvinht.moneyapp.base.input_money_in

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class InputMoneyInViewModelFactory(
    private val context: Context,
    private val date: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InputMoneyInViewModel(context, date) as T
    }
}