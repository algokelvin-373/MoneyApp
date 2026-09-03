package com.kelvinht.moneyapp.feature.home.presentation

import java.text.NumberFormat
import java.util.Locale

object RupiahFormatter {
    private val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))

    fun format(value: Long): String {
        return "Rp ${formatter.format(value)}"
    }

    fun formatSigned(value: Long): String {
        val prefix = if (value >= 0) "+Rp " else "-Rp "
        return prefix + formatter.format(kotlin.math.abs(value))
    }
}
