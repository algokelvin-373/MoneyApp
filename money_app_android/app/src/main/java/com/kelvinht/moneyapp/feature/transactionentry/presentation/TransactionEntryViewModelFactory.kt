package com.kelvinht.moneyapp.feature.transactionentry.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kelvinht.moneyapp.feature.transactionentry.data.DummyTransactionEntryRepository
import com.kelvinht.moneyapp.feature.transactionentry.domain.usecase.GetTransactionEntryOptionsUseCase

class TransactionEntryViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionEntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionEntryViewModel(GetTransactionEntryOptionsUseCase(DummyTransactionEntryRepository())) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
