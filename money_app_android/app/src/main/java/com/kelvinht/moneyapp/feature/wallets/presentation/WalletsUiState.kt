package com.kelvinht.moneyapp.feature.wallets.presentation

data class WalletsUiState(
    val userInitials: String,
    val accounts: List<WalletAccountUi>
)

data class WalletAccountUi(
    val name: String,
    val description: String,
    val balance: String,
    val style: WalletAccountStyle
)

enum class WalletAccountStyle {
    CASH,
    OVO,
    GOPAY,
    DANA
}
