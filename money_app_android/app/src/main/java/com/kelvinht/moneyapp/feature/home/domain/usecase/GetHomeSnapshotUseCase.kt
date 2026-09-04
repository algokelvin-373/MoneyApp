package com.kelvinht.moneyapp.feature.home.domain.usecase

import com.kelvinht.moneyapp.feature.home.domain.repository.HomeRepository

class GetHomeSnapshotUseCase(
    private val repository: HomeRepository
) {
    operator fun invoke() = repository.getHomeSnapshot()
}
