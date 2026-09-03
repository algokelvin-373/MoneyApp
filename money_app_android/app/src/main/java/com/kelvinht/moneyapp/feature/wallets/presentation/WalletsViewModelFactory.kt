package com.kelvinht.moneyapp.feature.wallets.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneyapp.feature.wallets.data.DummyWalletsRepository
import com.kelvinht.moneyapp.feature.wallets.domain.usecase.GetWalletsSnapshotUseCase

class WalletsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalletsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WalletsViewModel(GetWalletsSnapshotUseCase(DummyWalletsRepository())) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
