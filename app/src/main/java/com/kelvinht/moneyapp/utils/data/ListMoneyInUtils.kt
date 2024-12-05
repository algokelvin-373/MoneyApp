package com.kelvinht.moneyapp.utils.data

import android.content.Context
import android.graphics.Rect
import com.kelvinht.moneyapp.R
import com.kelvinht.moneyapp.data.MoneyIn
import com.kelvinht.moneyapp.data.MoneyInTitle
import com.kelvinht.moneyapp.data.MoneyInTotal

object ListMoneyInUtils {
    fun modifierListMoneyIn(
        context: Context,
        bounds: Rect,
        list: ArrayList<MoneyIn>,
    ): ArrayList<Any> {
        val result = ArrayList<Any>()
        val groupedTransactions = list.groupBy { it.date } // Make Group Based On Date
        groupedTransactions.entries.sortedByDescending {
            it.key // Sorted By Date Descending
        }.forEach { data ->
            val date = data.key
            val moneyIn = data.value
            val total = moneyIn.sumOf { it.amount }

            if (bounds.width() > bounds.height()) { // Mode Landscape
                // For Table Header
                val header = MoneyIn(0,
                    context.getString(R.string.moneyIn_into_to),
                    context.getString(R.string.moneyIn_data_from),
                    context.getString(R.string.moneyIn_description),
                    context.getString(R.string.empty_string),
                    context.getString(R.string.empty_string),
                    context.getString(R.string.moneyIn_time),
                    context.getString(R.string.empty_string),
                    -1)
                result.add(header)

                // For Data Money In
                result.add(MoneyInTitle(date = date)) // for data title date money in
                result.addAll(moneyIn) // for data money in based on date
                result.add(MoneyInTotal(date = date, totalAmount = total)) // for data total money in based on date
            } else { // Mode Portrait
                result.add(MoneyInTotal(date = date, totalAmount = total)) // for data total money in based on date
                result.addAll(moneyIn) // for data money in based on date
            }
        }
        return result
    }
}