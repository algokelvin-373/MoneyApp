package com.kelvinht.moneyapp.feature.home.domain.repository

import com.kelvinht.moneyapp.feature.home.domain.model.HomeSnapshot

interface HomeRepository {
    fun getHomeSnapshot(): HomeSnapshot
}
