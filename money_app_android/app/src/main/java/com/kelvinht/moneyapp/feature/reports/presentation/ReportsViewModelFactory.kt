package com.kelvinht.moneyapp.feature.reports.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneyapp.feature.reports.data.DummyReportsRepository
import com.kelvinht.moneyapp.feature.reports.domain.usecase.GetReportsSnapshotUseCase

class ReportsViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportsViewModel(GetReportsSnapshotUseCase(DummyReportsRepository())) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
