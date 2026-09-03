package com.kelvinht.moneyapp.feature.wallets.domain.model

data class WalletsSnapshot(
    val userInitials: String,
    val accounts: List<WalletAccount>
)

data class WalletAccount(
    val name: String,
    val description: String,
    val balance: Long,
    val type: WalletAccountType
)

enum class WalletAccountType {
    CASH,
    OVO,
    GOPAY,
    DANA
}
