package com.kelvinht.moneyapp.feature.reports.presentation

import androidx.lifecycle.ViewModel
import com.kelvinht.moneyapp.core.format.RupiahFormatter
import com.kelvinht.moneyapp.feature.reports.domain.model.ExpenseCategoryStyle
import com.kelvinht.moneyapp.feature.reports.domain.usecase.GetReportsSnapshotUseCase

class ReportsViewModel(
    private val getReportsSnapshot: GetReportsSnapshotUseCase
) : ViewModel() {
    fun loadReports(): ReportsUiState {
        val snapshot = getReportsSnapshot()
        return ReportsUiState(
            totalExpenseCompact = RupiahFormatter.formatCompactMillion(snapshot.totalExpense),
            categories = snapshot.categories.map {
                ExpenseCategoryUi(
                    name = it.name,
                    percentageLabel = "${it.percentage}%",
                    percentage = it.percentage,
                    style = when (it.style) {
                        ExpenseCategoryStyle.FOOD -> ExpenseCategoryUiStyle.FOOD
                        ExpenseCategoryStyle.TRANSPORT -> ExpenseCategoryUiStyle.TRANSPORT
                        ExpenseCategoryStyle.SAVING -> ExpenseCategoryUiStyle.SAVING
                    }
                )
            },
            remainingBudget = RupiahFormatter.format(snapshot.remainingBudget),
            totalBudget = "dari ${RupiahFormatter.format(snapshot.totalBudget)}",
            usedBudget = "Terpakai: ${RupiahFormatter.format(snapshot.usedBudget)}",
            budgetProgress = ((snapshot.usedBudget.toDouble() / snapshot.totalBudget.toDouble()) * 100).toInt(),
            budgetStatus = snapshot.budgetStatus,
            weeklyTipTitle = snapshot.weeklyTipTitle,
            weeklyTipDescription = snapshot.weeklyTipDescription
        )
    }
}
