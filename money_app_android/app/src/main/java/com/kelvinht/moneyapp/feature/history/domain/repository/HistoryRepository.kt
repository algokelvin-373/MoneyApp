package com.kelvinht.moneyapp.feature.history.domain.repository

import com.kelvinht.moneyapp.feature.history.domain.model.TransactionEntryOptions

interface HistoryRepository {
    fun getTransactionEntryOptions(): TransactionEntryOptions
}
