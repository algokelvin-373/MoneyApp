package com.kelvinht.moneylife.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyExpenseNotificationContentTest {
    @Test
    fun `shows completed date and total spending`() {
        val content = DailyExpenseNotificationContent.create("2026-11-27", 123_000L)

        assertEquals("Ringkasan Pengeluaran", content.title)
        assertEquals("27 Nov 2026 · Rp 123.000", content.message)
    }

    @Test
    fun `shows zero total for a day without expenses`() {
        val content = DailyExpenseNotificationContent.create("2026-11-27", 0L)

        assertEquals("27 Nov 2026 · Rp 0", content.message)
    }
}
