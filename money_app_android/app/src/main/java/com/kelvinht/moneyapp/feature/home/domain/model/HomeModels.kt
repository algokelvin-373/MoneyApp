package com.kelvinht.moneyapp.feature.home.domain.model

data class HomeSnapshot(
    val userName: String,
    val totalBalance: Long,
    val monthlyGrowth: Long,
    val wallets: List<HomeWallet>,
    val recentTransactions: List<HomeTransaction>
)

data class HomeWallet(
    val name: String,
    val balance: Long,
    val type: WalletType
)

enum class WalletType {
    CASH,
    E_WALLET,
    BANK
}

data class HomeTransaction(
    val title: String,
    val subtitle: String,
    val amount: Long,
    val type: TransactionDisplayType
)

enum class TransactionDisplayType {
    INCOME,
    EXPENSE,
    TRANSFER
}
