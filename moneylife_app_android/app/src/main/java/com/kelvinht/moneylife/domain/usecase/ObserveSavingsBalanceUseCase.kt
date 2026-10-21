package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.domain.repository.SavingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveSavingsBalanceUseCase(
    private val repository: SavingsRepository
) {
    operator fun invoke(monthKey: String): Flow<Long> = repository.observeBalance(monthKey)
}
