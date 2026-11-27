package com.kelvinht.moneylife.notification

import com.kelvinht.moneylife.core.format.RupiahFormatter
import com.kelvinht.moneylife.core.util.DateUtils

data class DailyExpenseNotificationContent(
    val title: String,
    val message: String,
) {
    companion object {
        fun create(dateKey: String, total: Long): DailyExpenseNotificationContent =
            DailyExpenseNotificationContent(
                title = "Ringkasan Pengeluaran",
                message = "${DateUtils.shortDisplayDate(dateKey)} · ${RupiahFormatter.format(total)}",
            )
    }
}
