package com.kelvinht.moneyapp.feature.wallets.presentation

import com.kelvinht.moneyapp.feature.wallets.domain.model.WalletAccount
import com.kelvinht.moneyapp.feature.wallets.domain.model.WalletAccountType
import com.kelvinht.moneyapp.feature.wallets.domain.model.WalletsSnapshot
import com.kelvinht.moneyapp.feature.wallets.domain.repository.WalletsRepository
import com.kelvinht.moneyapp.feature.wallets.domain.usecase.GetWalletsSnapshotUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class WalletsViewModelTest {
    @Test
    fun loadWallets_mapsAccountsIntoDisplayState() {
        val viewModel = WalletsViewModel(GetWalletsSnapshotUseCase(FakeWalletsRepository()))

        val state = viewModel.loadWallets()

        assertEquals("UP", state.userInitials)
        assertEquals(4, state.accounts.size)
        assertEquals("Uang Tunai", state.accounts[0].name)
        assertEquals("Dompet Utama", state.accounts[0].description)
        assertEquals("Rp 2.500.000", state.accounts[0].balance)
        assertEquals(WalletAccountStyle.CASH, state.accounts[0].style)
        assertEquals("OVO", state.accounts[1].name)
        assertEquals("Rp 850.000", state.accounts[1].balance)
        assertEquals(WalletAccountStyle.OVO, state.accounts[1].style)
        assertEquals(WalletAccountStyle.GOPAY, state.accounts[2].style)
        assertEquals(WalletAccountStyle.DANA, state.accounts[3].style)
    }
}

private class FakeWalletsRepository : WalletsRepository {
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
