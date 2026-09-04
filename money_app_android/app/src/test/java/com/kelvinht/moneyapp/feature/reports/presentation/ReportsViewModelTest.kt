package com.kelvinht.moneyapp.feature.reports.presentation

import com.kelvinht.moneyapp.feature.reports.domain.model.ExpenseCategory
import com.kelvinht.moneyapp.feature.reports.domain.model.ExpenseCategoryStyle
import com.kelvinht.moneyapp.feature.reports.domain.model.ReportsSnapshot
import com.kelvinht.moneyapp.feature.reports.domain.repository.ReportsRepository
import com.kelvinht.moneyapp.feature.reports.domain.usecase.GetReportsSnapshotUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportsViewModelTest {
    @Test
    fun loadReports_mapsReportDataIntoDisplayState() {
        val viewModel = ReportsViewModel(GetReportsSnapshotUseCase(FakeReportsRepository()))

        val state = viewModel.loadReports()

        assertEquals("Rp 4.5M", state.totalExpenseCompact)
        assertEquals(3, state.categories.size)
        assertEquals("Makanan", state.categories[0].name)
        assertEquals("40%", state.categories[0].percentageLabel)
        assertEquals(ExpenseCategoryUiStyle.FOOD, state.categories[0].style)
        assertEquals("Rp 2.500.000", state.remainingBudget)
        assertEquals("dari Rp 5.000.000", state.totalBudget)
        assertEquals("Terpakai: Rp 2.500.000", state.usedBudget)
        assertEquals(50, state.budgetProgress)
        assertEquals("Aman", state.budgetStatus)
        assertEquals("Tips Hemat Minggu Ini", state.weeklyTipTitle)
    }
}

private class FakeReportsRepository : ReportsRepository {
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
