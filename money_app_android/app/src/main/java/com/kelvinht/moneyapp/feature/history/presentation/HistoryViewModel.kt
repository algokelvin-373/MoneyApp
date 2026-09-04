package com.kelvinht.moneyapp.feature.history.presentation

import androidx.lifecycle.ViewModel
import com.kelvinht.moneyapp.core.format.RupiahFormatter
import com.kelvinht.moneyapp.feature.history.domain.model.HistoryGroup
import com.kelvinht.moneyapp.feature.history.domain.usecase.GetHistoryUseCase

class HistoryViewModel(private val getHistory: GetHistoryUseCase) : ViewModel() {
    private val snapshot by lazy { getHistory() }

    fun loadHistory(query: String = ""): HistoryUiState {
        val netCashFlow = snapshot.totalIncome - snapshot.totalExpense
        val filteredGroups = snapshot.groups.mapNotNull { filterGroup(it, query) }
        val shownCount = filteredGroups.sumOf { it.transactions.size }
        return HistoryUiState(
            monthLabel = snapshot.monthLabel,
            totalIncome = RupiahFormatter.formatSigned(snapshot.totalIncome),
            incomeCount = "${snapshot.incomeTransactionCount} transaksi",
            totalExpense = RupiahFormatter.formatSigned(-snapshot.totalExpense),
            expenseCount = "${snapshot.expenseTransactionCount} transaksi",
            netCashFlow = "${RupiahFormatter.formatSigned(netCashFlow)} (Surplus)",
            isSurplus = netCashFlow >= 0,
            incomeRatio = snapshot.totalIncome.toFloat() / (snapshot.totalIncome + snapshot.totalExpense),
            groups = filteredGroups.map { group ->
                HistoryGroupUi(group.dateLabel, RupiahFormatter.formatSigned(group.dailyTotal), group.transactions.map {
                    HistoryTransactionUi(
                        title = it.title,
                        category = it.category,
                        accountAndTime = "${it.account}  -  ${it.time}",
                        amount = RupiahFormatter.formatSigned(it.amount),
                        isIncome = it.amount > 0,
                        type = it.type
                    )
                })
            },
            displayInfo = if (query.isBlank()) {
                "Menampilkan ${snapshot.displayedTransactionCount} dari ${snapshot.totalTransactionCount} transaksi bulan ini"
            } else "$shownCount transaksi ditemukan",
            hasResults = shownCount > 0
        )
    }

    private fun filterGroup(group: HistoryGroup, query: String): HistoryGroup? {
        if (query.isBlank()) return group
        val filtered = group.transactions.filter {
            it.title.contains(query, true) || it.category.contains(query, true) || it.account.contains(query, true)
        }
        return if (filtered.isEmpty()) null else group.copy(transactions = filtered)
    }
}
