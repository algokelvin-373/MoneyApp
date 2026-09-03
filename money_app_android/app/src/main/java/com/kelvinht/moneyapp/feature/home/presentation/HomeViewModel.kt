package com.kelvinht.moneyapp.feature.home.presentation

import androidx.lifecycle.ViewModel
import com.kelvinht.moneyapp.feature.home.domain.model.TransactionDisplayType
import com.kelvinht.moneyapp.feature.home.domain.model.WalletType
import com.kelvinht.moneyapp.feature.home.domain.usecase.GetHomeSnapshotUseCase

class HomeViewModel(
    private val getHomeSnapshot: GetHomeSnapshotUseCase
) : ViewModel() {
    fun loadHome(): HomeUiState {
        val snapshot = getHomeSnapshot()
        return HomeUiState(
            greeting = "Halo, ${snapshot.userName}!",
            totalBalance = RupiahFormatter.format(snapshot.totalBalance),
            monthlyGrowth = "${RupiahFormatter.formatSigned(snapshot.monthlyGrowth)} bulan ini",
            wallets = snapshot.wallets.map {
                HomeWalletUi(
                    name = it.name,
                    balance = RupiahFormatter.format(it.balance),
                    style = when (it.type) {
                        WalletType.CASH -> WalletStyle.CASH
                        WalletType.E_WALLET -> WalletStyle.E_WALLET
                        WalletType.BANK -> WalletStyle.BANK
                    }
                )
            },
            transactions = snapshot.recentTransactions.map {
                HomeTransactionUi(
                    title = it.title,
                    subtitle = it.subtitle,
                    amount = RupiahFormatter.formatSigned(it.amount),
                    style = when (it.type) {
                        TransactionDisplayType.INCOME -> TransactionStyle.INCOME
                        TransactionDisplayType.EXPENSE -> TransactionStyle.EXPENSE
                        TransactionDisplayType.TRANSFER -> TransactionStyle.TRANSFER
                    }
                )
            }
        )
    }
}
