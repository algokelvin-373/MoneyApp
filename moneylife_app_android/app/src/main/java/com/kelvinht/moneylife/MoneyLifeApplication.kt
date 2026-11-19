package com.kelvinht.moneylife

import android.app.Application
import com.kelvinht.moneylife.di.AppContainer

class MoneyLifeApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
