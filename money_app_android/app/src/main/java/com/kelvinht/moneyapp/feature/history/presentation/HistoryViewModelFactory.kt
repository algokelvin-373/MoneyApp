package com.kelvinht.moneyapp.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneyapp.feature.history.data.DummyHistoryRepository
import com.kelvinht.moneyapp.feature.history.domain.usecase.GetHistoryUseCase

class HistoryViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(GetHistoryUseCase(DummyHistoryRepository())) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
