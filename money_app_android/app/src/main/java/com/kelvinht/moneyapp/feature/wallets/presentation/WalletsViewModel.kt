package com.kelvinht.moneyapp.feature.wallets.presentation

import androidx.lifecycle.ViewModel
import com.kelvinht.moneyapp.core.format.RupiahFormatter
import com.kelvinht.moneyapp.feature.wallets.domain.model.WalletAccountType
import com.kelvinht.moneyapp.feature.wallets.domain.usecase.GetWalletsSnapshotUseCase

class WalletsViewModel(
    private val getWalletsSnapshot: GetWalletsSnapshotUseCase
) : ViewModel() {
    fun loadWallets(): WalletsUiState {
        val snapshot = getWalletsSnapshot()
        return WalletsUiState(
            userInitials = snapshot.userInitials,
            accounts = snapshot.accounts.map {
                WalletAccountUi(
                    name = it.name,
                    description = it.description,
                    balance = RupiahFormatter.format(it.balance),
                    style = when (it.type) {
                        WalletAccountType.CASH -> WalletAccountStyle.CASH
                        WalletAccountType.OVO -> WalletAccountStyle.OVO
                        WalletAccountType.GOPAY -> WalletAccountStyle.GOPAY
                        WalletAccountType.DANA -> WalletAccountStyle.DANA
                    }
                )
            }
        )
    }
}
