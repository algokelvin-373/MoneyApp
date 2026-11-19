package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.domain.model.DailyBudget
import com.kelvinht.moneylife.domain.repository.DailyLimitRepository

class SetDailyLimitUseCase(
    private val repository: DailyLimitRepository,
) {
    suspend operator fun invoke(limit: Long) {
        require(DailyBudget.isSupportedLimit(limit)) { "Unsupported daily limit: $limit" }
        repository.setDailyLimit(limit)
    }
}
