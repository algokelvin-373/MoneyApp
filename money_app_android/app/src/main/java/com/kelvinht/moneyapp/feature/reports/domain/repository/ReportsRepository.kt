package com.kelvinht.moneyapp.feature.reports.domain.repository

import com.kelvinht.moneyapp.feature.reports.domain.model.ReportsSnapshot

interface ReportsRepository {
    fun getReportsSnapshot(): ReportsSnapshot
}
