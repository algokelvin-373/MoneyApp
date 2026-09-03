package com.kelvinht.moneyapp.feature.home.data

import com.kelvinht.moneyapp.feature.home.domain.model.HomeSnapshot
import com.kelvinht.moneyapp.feature.home.domain.model.HomeTransaction
import com.kelvinht.moneyapp.feature.home.domain.model.HomeWallet
import com.kelvinht.moneyapp.feature.home.domain.model.TransactionDisplayType
import com.kelvinht.moneyapp.feature.home.domain.model.WalletType
import com.kelvinht.moneyapp.feature.home.domain.repository.HomeRepository

class DummyHomeRepository : HomeRepository {
    override fun getHomeSnapshot(): HomeSnapshot {
        return HomeSnapshot(
            userName = "Budi",
            totalBalance = 15_450_000,
            monthlyGrowth = 2_100_000,
            wallets = listOf(
                HomeWallet(name = "Tunai", balance = 500_000, type = WalletType.CASH),
                HomeWallet(name = "GoPay", balance = 2_350_000, type = WalletType.E_WALLET),
                HomeWallet(name = "Bank", balance = 12_600_000, type = WalletType.BANK)
            ),
            recentTransactions = listOf(
                HomeTransaction(
                    title = "Makan Siang",
                    subtitle = "Hari ini, 12:30 - Tunai",
                    amount = -45_000,
                    type = TransactionDisplayType.EXPENSE
                ),
                HomeTransaction(
                    title = "Top up GoPay",
                    subtitle = "Kemarin, 09:15 - Bank",
                    amount = -500_000,
                    type = TransactionDisplayType.TRANSFER
                ),
                HomeTransaction(
                    title = "Penerimaan Gaji",
                    subtitle = "25 Okt 2023 - Bank",
                    amount = 12_000_000,
                    type = TransactionDisplayType.INCOME
                )
            )
        )
    }
}
