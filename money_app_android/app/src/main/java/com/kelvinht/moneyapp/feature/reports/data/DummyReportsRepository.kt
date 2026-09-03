package com.kelvinht.moneyapp.feature.reports.data

import com.kelvinht.moneyapp.feature.reports.domain.model.ExpenseCategory
import com.kelvinht.moneyapp.feature.reports.domain.model.ExpenseCategoryStyle
import com.kelvinht.moneyapp.feature.reports.domain.model.ReportsSnapshot
import com.kelvinht.moneyapp.feature.reports.domain.repository.ReportsRepository

class DummyReportsRepository : ReportsRepository {
    override fun getReportsSnapshot(): ReportsSnapshot {
        return ReportsSnapshot(
            totalExpense = 4_500_000,
            categories = listOf(
                ExpenseCategory("Makanan", 40, ExpenseCategoryStyle.FOOD),
                ExpenseCategory("Transportasi", 20, ExpenseCategoryStyle.TRANSPORT),
                ExpenseCategory("Tabungan", 10, ExpenseCategoryStyle.SAVING)
            ),
            remainingBudget = 2_500_000,
            totalBudget = 5_000_000,
            usedBudget = 2_500_000,
            budgetStatus = "Aman",
            weeklyTipTitle = "Tips Hemat Minggu Ini",
            weeklyTipDescription = "Kurangi pengeluaran makan di luar dan alihkan ke tabungan."
        )
    }
}
