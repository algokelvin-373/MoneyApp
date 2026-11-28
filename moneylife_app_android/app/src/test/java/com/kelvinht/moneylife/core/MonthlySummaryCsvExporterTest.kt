package com.kelvinht.moneylife.core

import com.kelvinht.moneylife.core.export.MonthlySummaryCsvExporter
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.model.MonthlyExpenseSummary
import org.junit.Assert.assertTrue
import org.junit.Test

class MonthlySummaryCsvExporterTest {
    @Test
    fun exportsMonthlyTotalsDailyTotalsAndEveryCategory() {
        val summary =
            MonthlyExpenseSummary(
                totalExpenses = 15_000L,
                categoryTotals =
                    ExpenseCategory.entries.associateWith { category ->
                        if (category == ExpenseCategory.MAKAN) 15_000L else 0L
                    },
                dailyTotals = mapOf("2026-11-01" to 15_000L),
                totalSavings = 7_000L,
            )

        val csv = MonthlySummaryCsvExporter.toCsv("2026-11", summary)

        assertTrue(csv.contains("\"TOTAL_PENGELUARAN\",\"\",\"15000\""))
        assertTrue(csv.contains("\"TOTAL_TABUNGAN\",\"\",\"7000\""))
        assertTrue(csv.contains("\"PENGELUARAN_HARIAN\",\"2026-11-01\",\"15000\""))
        assertTrue(csv.contains("\"PENGELUARAN_KATEGORI\",\"Makan\",\"15000\""))
        assertTrue(csv.contains("\"PENGELUARAN_KATEGORI\",\"Transport\",\"0\""))
        assertTrue(csv.contains("\"PENGELUARAN_KATEGORI\",\"Uang Amal\",\"0\""))
    }
}
