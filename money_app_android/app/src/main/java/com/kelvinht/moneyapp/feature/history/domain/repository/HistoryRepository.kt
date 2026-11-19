package com.kelvinht.moneyapp.feature.history.domain.repository

import com.kelvinht.moneyapp.feature.history.domain.model.HistorySnapshot

interface HistoryRepository {
    fun getHistory(): HistorySnapshot
}
