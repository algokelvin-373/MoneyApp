package com.kelvinht.moneyapp.feature.history.data

import com.kelvinht.moneyapp.feature.history.domain.model.TransactionEntryOptions
import com.kelvinht.moneyapp.feature.history.domain.repository.HistoryRepository

class DummyHistoryRepository : HistoryRepository {
    override fun getTransactionEntryOptions(): TransactionEntryOptions {
        return TransactionEntryOptions(
            categories = listOf("Makanan", "Transportasi", "Belanja", "Tagihan"),
            fundingSources = listOf("Tunai", "OVO", "GoPay"),
            selectedCategory = "Pilih Kategori",
            selectedSource = "Tunai",
            datePlaceholder = "mm/dd/yyyy"
        )
    }
}
