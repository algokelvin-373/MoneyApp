package com.kelvinht.moneyapp.feature.history.domain.usecase

import com.kelvinht.moneyapp.feature.history.domain.repository.HistoryRepository

class GetHistoryUseCase(private val repository: HistoryRepository) {
    operator fun invoke() = repository.getHistory()
}
