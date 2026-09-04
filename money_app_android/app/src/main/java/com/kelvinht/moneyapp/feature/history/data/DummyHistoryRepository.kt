package com.kelvinht.moneyapp.feature.history.data

import com.kelvinht.moneyapp.feature.history.domain.model.HistoryGroup
import com.kelvinht.moneyapp.feature.history.domain.model.HistorySnapshot
import com.kelvinht.moneyapp.feature.history.domain.model.HistoryTransaction
import com.kelvinht.moneyapp.feature.history.domain.model.HistoryTransactionType
import com.kelvinht.moneyapp.feature.history.domain.repository.HistoryRepository

class DummyHistoryRepository : HistoryRepository {
    override fun getHistory() = HistorySnapshot(
        monthLabel = "Okt 2023",
        totalIncome = 4_250_000,
        incomeTransactionCount = 4,
        totalExpense = 1_480_000,
        expenseTransactionCount = 30,
        displayedTransactionCount = 8,
        totalTransactionCount = 34,
        groups = listOf(
            HistoryGroup("HARI INI - 24 OKT 2023", -55_000, listOf(
                transaction("Makan Siang Warteg E", "FOOD", "Dompet (Tunai)", "12:45", -28_000, HistoryTransactionType.FOOD),
                transaction("Kopi Kenangan Manta", "FOOD", "GoPay", "09:15", -22_000, HistoryTransactionType.FOOD),
                transaction("Parkir Motor Stasiun", "TRN", "Dompet (Tunai)", "07:30", -5_000, HistoryTransactionType.TRANSPORT)
            )),
            HistoryGroup("KEMARIN - 23 OKT 2023", 3_670_000, listOf(
                transaction("Gaji Pokok PT Tek", "INC", "Dompet Utama", "15:00", 4_000_000, HistoryTransactionType.INCOME),
                transaction("Top Up Saldo OVO", "TRANSFER", "GoPay -> OVO", "16:30", -150_000, HistoryTransactionType.TRANSFER),
                transaction("Belanja Superindo Ex", "SHOP", "OVO", "19:10", -180_000, HistoryTransactionType.SHOPPING)
            )),
            HistoryGroup("21 OKT 2023", -20_000, listOf(
                transaction("Cashback Promo OVO", "CASHBACK", "OVO", "11:20", 15_000, HistoryTransactionType.CASHBACK),
                transaction("Bensin Pertalite Motor", "TRN", "Dompet (Tunai)", "08:00", -35_000, HistoryTransactionType.TRANSPORT)
            ))
        )
    )

    private fun transaction(title: String, category: String, account: String, time: String, amount: Long, type: HistoryTransactionType) =
        HistoryTransaction(title, category, account, time, amount, type)
}
