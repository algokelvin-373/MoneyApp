package com.kelvinht.moneyapp.feature.transactionentry.domain.repository

import com.kelvinht.moneyapp.feature.transactionentry.domain.model.TransactionEntryOptions

interface TransactionEntryRepository {
    fun getOptions(): TransactionEntryOptions
}
