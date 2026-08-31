package com.kelvinht.moneyapp.feature.finance.domain.usecase

import com.kelvinht.moneyapp.feature.finance.domain.repository.FinanceRepository

class GetDashboardSummaryUseCase(
    private val repository: FinanceRepository
) {
    suspend operator fun invoke(token: String) = repository.getDashboardSummary(token)
}
