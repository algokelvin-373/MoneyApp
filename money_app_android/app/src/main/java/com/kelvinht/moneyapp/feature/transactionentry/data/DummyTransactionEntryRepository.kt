package com.kelvinht.moneyapp.feature.transactionentry.data

import com.kelvinht.moneyapp.feature.transactionentry.domain.model.TransactionEntryOptions
import com.kelvinht.moneyapp.feature.transactionentry.domain.repository.TransactionEntryRepository

class DummyTransactionEntryRepository : TransactionEntryRepository {
    override fun getOptions() = TransactionEntryOptions(
        categories = listOf("Makan", "Transport", "Jajan"),
        fundingSources = listOf("Tunai"),
        selectedCategory = "Makan",
        selectedSource = "Tunai",
        transactionDate = "Hari ini, 14:20 WIB",
        note = "Makan siang Rendang & Es Teh",
        initialAmount = 45_000
    )
}
