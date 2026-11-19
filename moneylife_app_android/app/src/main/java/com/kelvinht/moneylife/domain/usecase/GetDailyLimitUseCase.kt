package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.domain.repository.DailyLimitRepository

class GetDailyLimitUseCase(
    private val repository: DailyLimitRepository,
) {
    suspend operator fun invoke(dateKey: String): Long = repository.getDailyLimit(dateKey)
}
