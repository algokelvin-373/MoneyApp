package com.kelvinht.moneyapp.feature.transactionentry.data

import com.kelvinht.moneyapp.feature.transactionentry.domain.model.TransactionEntryOptions
import com.kelvinht.moneyapp.feature.transactionentry.domain.repository.TransactionEntryRepository

class DummyTransactionEntryRepository : TransactionEntryRepository {
    override fun getOptions() = TransactionEntryOptions(
        categories = listOf("Makanan", "Transportasi", "Belanja", "Tagihan"),
        fundingSources = listOf("Tunai", "OVO", "GoPay"),
        selectedCategory = "Pilih Kategori",
        selectedSource = "Tunai",
        datePlaceholder = "mm/dd/yyyy"
    )
}
