package com.kelvinht.moneyapp.feature.history.presentation

import com.kelvinht.moneyapp.feature.history.domain.model.HistoryGroup
import com.kelvinht.moneyapp.feature.history.domain.model.HistorySnapshot
import com.kelvinht.moneyapp.feature.history.domain.model.HistoryTransaction
import com.kelvinht.moneyapp.feature.history.domain.model.HistoryTransactionType
import com.kelvinht.moneyapp.feature.history.domain.repository.HistoryRepository
import com.kelvinht.moneyapp.feature.history.domain.usecase.GetHistoryUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryViewModelTest {
    @Test
    fun loadHistory_mapsSummaryAndGroupedTransactions() {
        val state = viewModel().loadHistory()

        assertEquals("Okt 2023", state.monthLabel)
        assertEquals("+Rp 4.250.000", state.totalIncome)
        assertEquals("-Rp 1.480.000", state.totalExpense)
        assertEquals("+Rp 2.770.000 (Surplus)", state.netCashFlow)
        assertTrue(state.isSurplus)
        assertEquals(2, state.groups.size)
        assertEquals("-Rp 28.000", state.groups.first().transactions.first().amount)
        assertEquals("Menampilkan 3 dari 34 transaksi bulan ini", state.displayInfo)
    }

    @Test
    fun search_filtersByMerchantCategoryAndAccount() {
        val viewModel = viewModel()

        val merchant = viewModel.loadHistory("kopi")
        assertEquals(1, merchant.groups.single().transactions.size)
        assertEquals("Kopi Kenangan", merchant.groups.single().transactions.single().title)

        val account = viewModel.loadHistory("OVO")
        assertEquals(1, account.groups.single().transactions.size)
        assertEquals("1 transaksi ditemukan", account.displayInfo)

        val empty = viewModel.loadHistory("tidak ada")
        assertFalse(empty.hasResults)
        assertTrue(empty.groups.isEmpty())
    }

    private fun viewModel() = HistoryViewModel(GetHistoryUseCase(FakeHistoryRepository()))
}

private class FakeHistoryRepository : HistoryRepository {
    override fun getHistory() = HistorySnapshot(
        monthLabel = "Okt 2023",
        totalIncome = 4_250_000,
        incomeTransactionCount = 4,
        totalExpense = 1_480_000,
        expenseTransactionCount = 30,
        displayedTransactionCount = 3,
        totalTransactionCount = 34,
        groups = listOf(
            HistoryGroup("HARI INI", -50_000, listOf(
                HistoryTransaction("Makan Siang", "FOOD", "Tunai", "12:45", -28_000, HistoryTransactionType.FOOD),
                HistoryTransaction("Kopi Kenangan", "FOOD", "GoPay", "09:15", -22_000, HistoryTransactionType.FOOD)
            )),
            HistoryGroup("KEMARIN", 4_000_000, listOf(
                HistoryTransaction("Gaji", "INC", "OVO", "15:00", 4_000_000, HistoryTransactionType.INCOME)
            ))
        )
    )
}
