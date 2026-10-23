package com.kelvinht.moneylife.presentation.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneylife.di.AppContainer

class AddExpenseViewModelFactory(
    private val container: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddExpenseViewModel(container.addExpense) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
