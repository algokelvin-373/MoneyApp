package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.domain.repository.DailyLimitRepository

/** Locks in the daily limit of [dateKey] once that day is no longer editable. */
class SnapshotDailyLimitUseCase(
    private val repository: DailyLimitRepository,
) {
    suspend operator fun invoke(dateKey: String) = repository.snapshotDailyLimit(dateKey)
}
