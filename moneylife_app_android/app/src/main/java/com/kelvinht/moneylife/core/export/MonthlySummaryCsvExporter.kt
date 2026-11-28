package com.kelvinht.moneylife.core.export

import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.model.MonthlyExpenseSummary

object MonthlySummaryCsvExporter {
    fun toCsv(
        monthKey: String,
        summary: MonthlyExpenseSummary,
    ): String =
        buildList {
            add(listOf("Jenis", "Rincian", "Nominal (Rp)"))
            add(listOf("BULAN", monthKey, ""))
            add(listOf("TOTAL_PENGELUARAN", "", summary.totalExpenses.toString()))
            add(listOf("TOTAL_TABUNGAN", "", summary.totalSavings.toString()))
            summary.dailyTotals.forEach { (dateKey, amount) ->
                add(listOf("PENGELUARAN_HARIAN", dateKey, amount.toString()))
            }
            ExpenseCategory.entries.forEach { category ->
                add(
                    listOf(
                        "PENGELUARAN_KATEGORI",
                        category.displayName(),
                        (summary.categoryTotals[category] ?: 0L).toString(),
                    ),
                )
            }
        }.joinToString(separator = "\r\n") { row -> row.joinToString(",") { it.escapeCsv() } } + "\r\n"

    private fun ExpenseCategory.displayName(): String =
        when (this) {
            ExpenseCategory.MAKAN -> "Makan"
            ExpenseCategory.TRANSPORT -> "Transport"
            ExpenseCategory.JAJAN -> "Jajan"
            ExpenseCategory.KEBUTUHAN_LAIN -> "Kebutuhan lain"
            ExpenseCategory.KEBUTUHAN_ORANG_TUA -> "Kebutuhan orang tua"
            ExpenseCategory.TOP_UP_COINS_TIKTOK -> "Top Up Coins TikTok"
            ExpenseCategory.UANG_AMAL -> "Uang Amal"
        }

    private fun String.escapeCsv(): String = "\"${replace("\"", "\"\"")}\""
}
