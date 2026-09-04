package com.kelvinht.moneyapp.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneyapp.feature.home.data.DummyHomeRepository
import com.kelvinht.moneyapp.feature.home.domain.usecase.GetHomeSnapshotUseCase

class HomeViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(GetHomeSnapshotUseCase(DummyHomeRepository())) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
