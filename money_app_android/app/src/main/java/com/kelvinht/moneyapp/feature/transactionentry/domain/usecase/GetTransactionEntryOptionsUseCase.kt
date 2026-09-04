package com.kelvinht.moneyapp.feature.transactionentry.domain.usecase

import com.kelvinht.moneyapp.feature.transactionentry.domain.repository.TransactionEntryRepository

class GetTransactionEntryOptionsUseCase(private val repository: TransactionEntryRepository) {
    operator fun invoke() = repository.getOptions()
}
