package com.kelvinht.moneyapp.feature.reports.domain.usecase

import com.kelvinht.moneyapp.feature.reports.domain.repository.ReportsRepository

class GetReportsSnapshotUseCase(
    private val repository: ReportsRepository
) {
    operator fun invoke() = repository.getReportsSnapshot()
}
