package com.kelvinht.moneyapp.utils

import java.text.NumberFormat
import java.util.Locale

object GlobalFunction {
    fun formatRupiah(amount: Int): String {
        val localeID = Locale("id", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(amount)
    }
}