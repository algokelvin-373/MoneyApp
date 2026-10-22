package com.kelvinht.moneylife.core.format

import java.text.NumberFormat
import java.util.Locale

/**
 * Formats numbers as Indonesian Rupiah for display.
 */
object RupiahFormatter {
    private val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))

    /** Example: 100000 -> "Rp 100.000" */
    fun format(value: Long): String = "Rp ${formatter.format(value)}"

    /** Example: 100000 -> "100.000" */
    fun formatDigits(value: Long): String = formatter.format(value)
}
