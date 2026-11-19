package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.domain.repository.DailyLimitRepository
import kotlinx.coroutines.flow.Flow

class ObserveDailyLimitUseCase(
    private val repository: DailyLimitRepository,
) {
    operator fun invoke(dateKey: String): Flow<Long> = repository.observeDailyLimit(dateKey)
}
