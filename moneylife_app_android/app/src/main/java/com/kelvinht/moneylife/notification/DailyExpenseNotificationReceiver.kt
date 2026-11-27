package com.kelvinht.moneylife.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kelvinht.moneylife.MoneyLifeApplication
import com.kelvinht.moneylife.R
import com.kelvinht.moneylife.presentation.home.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DailyExpenseNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val dateKey = intent.getStringExtra(DailyExpenseNotificationScheduler.EXTRA_DATE_KEY)
            ?: DailyExpenseNotificationScheduler.dateKeyForYesterday()
        val pendingResult = goAsync()
        val app = context.applicationContext as MoneyLifeApplication

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val expenses = app.container.getDailyExpenses(dateKey).first()
                DailyExpenseNotifier.show(context, dateKey, expenses.sumOf { it.amount })
            } catch (exception: Exception) {
                // A transient database or notification error must not prevent
                // the next day's reminder from being scheduled.
                Log.e(TAG, "Failed to create daily expense notification", exception)
            } finally {
                try {
                    DailyExpenseNotificationScheduler.scheduleNext(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private companion object {
        const val TAG = "DailyExpenseNotify"
    }
}

class DailyExpenseAlarmRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        DailyExpenseNotificationScheduler.scheduleNext(context)
    }
}

private object DailyExpenseNotifier {
    private const val CHANNEL_ID = "daily_expense_summary"
    private const val CHANNEL_NAME = "Ringkasan pengeluaran harian"

    fun show(
        context: Context,
        dateKey: String,
        total: Long,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }

        val content = DailyExpenseNotificationContent.create(dateKey, total)
        val openAppIntent = Intent(context, MainActivity::class.java)
        val contentIntent =
            PendingIntent.getActivity(
                context,
                1,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_moneylife)
                .setContentTitle(content.title)
                .setContentText(content.message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content.message))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        NotificationManagerCompat.from(context).notify(dateKey, 0, notification)
    }
}
