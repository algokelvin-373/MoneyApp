package com.kelvinht.moneyapp.feature.home.presentation

data class HomeUiState(
    val greeting: String,
    val totalBalance: String,
    val monthlyGrowth: String,
    val wallets: List<HomeWalletUi>,
    val transactions: List<HomeTransactionUi>
)

data class HomeWalletUi(
    val name: String,
    val balance: String,
    val style: WalletStyle
)

enum class WalletStyle {
    CASH,
    E_WALLET,
    BANK
}

data class HomeTransactionUi(
    val title: String,
    val subtitle: String,
    val amount: String,
    val style: TransactionStyle
)

enum class TransactionStyle {
    INCOME,
    EXPENSE,
    TRANSFER
}
