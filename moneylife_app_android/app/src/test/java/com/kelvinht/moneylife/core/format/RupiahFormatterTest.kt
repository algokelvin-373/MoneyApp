package com.kelvinht.moneylife.core.format

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Guards the rupiah formatting used by the presentation layer.
 */
class RupiahFormatterTest {
    @Test
    fun `formats full rupiah values`() {
        assertEquals("Rp 0", RupiahFormatter.format(0L))
        assertEquals("Rp 100.000", RupiahFormatter.format(100_000L))
    }

    @Test
    fun `formats digits without the currency prefix`() {
        assertEquals("45.000", RupiahFormatter.formatDigits(45_000L))
    }

    @Test
    fun `formats compact labels`() {
        assertEquals("999", RupiahFormatter.formatCompact(999L))
        assertEquals("50k", RupiahFormatter.formatCompact(50_000L))
        assertEquals("1,5k", RupiahFormatter.formatCompact(1_500L))
    }
}
