package com.kelvinht.moneyapp.core.format

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

object RupiahFormatter {
    private val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))

    fun format(value: Long): String {
        return "Rp ${formatter.format(value)}"
    }

    fun formatCompactMillion(value: Long): String {
        if (value % 1_000_000 == 0L) {
            return "Rp ${value / 1_000_000}M"
        }
        return "Rp ${String.format(Locale.US, "%.1f", value / 1_000_000.0)}M"
    }

    fun formatSigned(value: Long): String {
        val prefix = if (value >= 0) "+Rp " else "-Rp "
        return prefix + formatter.format(abs(value))
    }
}
