package com.kelvinht.moneyapp.feature.home.presentation

import com.kelvinht.moneyapp.core.format.RupiahFormatter
import com.kelvinht.moneyapp.feature.home.domain.model.HomeSnapshot
import com.kelvinht.moneyapp.feature.home.domain.model.HomeTransaction
import com.kelvinht.moneyapp.feature.home.domain.model.HomeWallet
import com.kelvinht.moneyapp.feature.home.domain.model.TransactionDisplayType
import com.kelvinht.moneyapp.feature.home.domain.model.WalletType
import com.kelvinht.moneyapp.feature.home.domain.repository.HomeRepository
import com.kelvinht.moneyapp.feature.home.domain.usecase.GetHomeSnapshotUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {
    @Test
    fun loadHome_mapsSnapshotIntoDisplayState() {
        val viewModel = HomeViewModel(GetHomeSnapshotUseCase(FakeHomeRepository()))

        val state = viewModel.loadHome()

        assertEquals("Halo, Budi!", state.greeting)
        assertEquals("Rp 15.450.000", state.totalBalance)
        assertEquals("+Rp 2.100.000 bulan ini", state.monthlyGrowth)
        assertEquals(3, state.wallets.size)
        assertEquals("Tunai", state.wallets[0].name)
        assertEquals("Rp 500.000", state.wallets[0].balance)
        assertEquals(WalletStyle.CASH, state.wallets[0].style)
        assertEquals("GoPay", state.wallets[1].name)
        assertEquals(WalletStyle.E_WALLET, state.wallets[1].style)
        assertEquals(3, state.transactions.size)
        assertEquals("-Rp 45.000", state.transactions[0].amount)
        assertEquals(TransactionStyle.EXPENSE, state.transactions[0].style)
        assertEquals("-Rp 500.000", state.transactions[1].amount)
        assertEquals(TransactionStyle.TRANSFER, state.transactions[1].style)
        assertEquals("+Rp 12.000.000", state.transactions[2].amount)
        assertEquals(TransactionStyle.INCOME, state.transactions[2].style)
    }

    @Test
    fun rupiahFormatter_formatsPositiveAndNegativeValues() {
        assertEquals("Rp 15.450.000", RupiahFormatter.format(15_450_000))
        assertEquals("+Rp 2.100.000", RupiahFormatter.formatSigned(2_100_000))
        assertEquals("-Rp 45.000", RupiahFormatter.formatSigned(-45_000))
    }
}

private class FakeHomeRepository : HomeRepository {
    override fun getHomeSnapshot(): HomeSnapshot {
        return HomeSnapshot(
            userName = "Budi",
            totalBalance = 15_450_000,
            monthlyGrowth = 2_100_000,
            wallets = listOf(
                HomeWallet("Tunai", 500_000, WalletType.CASH),
                HomeWallet("GoPay", 2_350_000, WalletType.E_WALLET),
                HomeWallet("Bank", 12_600_000, WalletType.BANK)
            ),
            recentTransactions = listOf(
                HomeTransaction("Makan Siang", "Hari ini, 12:30 - Tunai", -45_000, TransactionDisplayType.EXPENSE),
                HomeTransaction("Top up GoPay", "Kemarin, 09:15 - Bank", -500_000, TransactionDisplayType.TRANSFER),
                HomeTransaction("Penerimaan Gaji", "25 Okt 2023 - Bank", 12_000_000, TransactionDisplayType.INCOME)
            )
        )
    }
}
