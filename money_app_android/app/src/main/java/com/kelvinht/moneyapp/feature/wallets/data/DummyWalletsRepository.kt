package com.kelvinht.moneyapp.feature.wallets.data

import com.kelvinht.moneyapp.feature.wallets.domain.model.WalletAccount
import com.kelvinht.moneyapp.feature.wallets.domain.model.WalletAccountType
import com.kelvinht.moneyapp.feature.wallets.domain.model.WalletsSnapshot
import com.kelvinht.moneyapp.feature.wallets.domain.repository.WalletsRepository

class DummyWalletsRepository : WalletsRepository {
    override fun getWalletsSnapshot(): WalletsSnapshot {
        return WalletsSnapshot(
            userInitials = "UP",
            accounts = listOf(
                WalletAccount("Uang Tunai", "Dompet Utama", 2_500_000, WalletAccountType.CASH),
                WalletAccount("OVO", "E-Wallet", 850_000, WalletAccountType.OVO),
                WalletAccount("GoPay", "E-Wallet", 1_200_000, WalletAccountType.GOPAY),
                WalletAccount("Dana", "E-Wallet", 450_000, WalletAccountType.DANA)
            )
        )
    }
}
