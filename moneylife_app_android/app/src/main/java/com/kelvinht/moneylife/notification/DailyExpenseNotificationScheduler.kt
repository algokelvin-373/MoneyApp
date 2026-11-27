package com.kelvinht.moneylife.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kelvinht.moneylife.core.util.DateUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object DailyExpenseNotificationScheduler {
    const val EXTRA_DATE_KEY = "com.kelvinht.moneylife.extra.DAILY_EXPENSE_DATE_KEY"

    fun scheduleNext(context: Context) {
        val now = LocalDateTime.now()
        val dateKey = DateUtils.dateKey(now.toLocalDate())
        val triggerAt =
            now.toLocalDate()
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = notificationPendingIntent(context, dateKey)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun dateKeyForYesterday(): String = DateUtils.dateKey(LocalDate.now().minusDays(1))

    private fun notificationPendingIntent(
        context: Context,
        dateKey: String,
    ): PendingIntent {
        val intent =
            Intent(context, DailyExpenseNotificationReceiver::class.java)
                .putExtra(EXTRA_DATE_KEY, dateKey)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
