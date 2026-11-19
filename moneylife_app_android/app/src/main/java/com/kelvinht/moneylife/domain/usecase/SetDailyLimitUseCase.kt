package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.model.DailyBudget
import com.kelvinht.moneylife.domain.repository.DailyLimitRepository

class SetDailyLimitUseCase(
    private val repository: DailyLimitRepository,
) {
    /** Returns false when the request targets a date that is no longer editable. */
    suspend operator fun invoke(
        dateKey: String,
        limit: Long,
    ): Boolean {
        if (dateKey != DateUtils.todayKey()) return false
        if (!DailyBudget.isSupportedLimit(limit)) return false
        repository.setDailyLimit(dateKey, limit)
        return true
    }
}
