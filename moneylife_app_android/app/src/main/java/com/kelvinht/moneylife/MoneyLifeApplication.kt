package com.kelvinht.moneylife

import android.app.Application
import com.kelvinht.moneylife.di.AppContainer
import com.kelvinht.moneylife.notification.DailyExpenseNotificationScheduler

class MoneyLifeApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        DailyExpenseNotificationScheduler.scheduleNext(this)
    }
}
