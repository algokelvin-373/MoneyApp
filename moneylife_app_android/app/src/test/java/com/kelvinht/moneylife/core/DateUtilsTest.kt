package com.kelvinht.moneylife.core

import com.kelvinht.moneylife.core.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {
    @Test
    fun `short display date uses abbreviated month`() {
        assertEquals("9 Okt 2026", DateUtils.shortDisplayDate("2026-10-09"))
    }

    @Test
    fun `display date uses abbreviated month like the mockup`() {
        assertEquals("Jumat, 9 Okt 2026", DateUtils.displayDate("2026-10-09"))
    }

    @Test
    fun `parses and re-serialises the same date key`() {
        val date = DateUtils.parseDate("2026-10-09")
        assertEquals("2026-10-09", DateUtils.dateKey(date))
    }

    @Test
    fun `month label uses abbreviated month and year`() {
        assertEquals("Okt 2026", DateUtils.monthLabel("2026-10-09"))
    }

    @Test
    fun `today or past accepts today and earlier dates but rejects tomorrow`() {
        val today = LocalDate.now()
        assertTrue(DateUtils.isTodayOrPast(DateUtils.dateKey(today)))
        assertTrue(DateUtils.isTodayOrPast(DateUtils.dateKey(today.minusDays(1))))
        assertFalse(DateUtils.isTodayOrPast(DateUtils.dateKey(today.plusDays(1))))
    }

    @Test
    fun `past rejects today and future dates`() {
        val today = LocalDate.now()
        assertTrue(DateUtils.isPast(DateUtils.dateKey(today.minusDays(1))))
        assertFalse(DateUtils.isPast(DateUtils.dateKey(today)))
        assertFalse(DateUtils.isPast(DateUtils.dateKey(today.plusDays(1))))
    }
}
