package com.kelvinht.moneylife.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneylife.di.AppContainer

class HomeViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                getDailyExpenses = container.getDailyExpenses,
                calculateDailyBudget = container.calculateDailyBudget,
                deleteExpense = container.deleteExpense,
                observeSavingsBalance = container.observeSavingsBalance,
                rolloverDailySavings = container.rolloverDailySavings,
                dailyLimit = container.dailyLimit
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
