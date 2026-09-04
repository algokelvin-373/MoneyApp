package com.kelvinht.moneyapp.feature.wallets.domain.repository

import com.kelvinht.moneyapp.feature.wallets.domain.model.WalletsSnapshot

interface WalletsRepository {
    fun getWalletsSnapshot(): WalletsSnapshot
}
