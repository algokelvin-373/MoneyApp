package com.kelvinht.moneylife.core

import com.kelvinht.moneylife.core.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class DateUtilsTest {

    @Test
    fun `short display date uses abbreviated month`() {
        assertEquals("9 Okt 2026", DateUtils.shortDisplayDate("2026-10-09"))
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
}
