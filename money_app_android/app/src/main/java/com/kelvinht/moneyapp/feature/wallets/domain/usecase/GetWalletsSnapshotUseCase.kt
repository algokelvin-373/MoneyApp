package com.kelvinht.moneyapp.feature.wallets.domain.usecase

import com.kelvinht.moneyapp.feature.wallets.domain.repository.WalletsRepository

class GetWalletsSnapshotUseCase(
    private val repository: WalletsRepository
) {
    operator fun invoke() = repository.getWalletsSnapshot()
}
