package com.kelvinht.moneylife.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinht.moneylife.core.format.RupiahFormatter
import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.model.DailyBudget
import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.usecase.CalculateDailyBudgetUseCase
import com.kelvinht.moneylife.domain.usecase.DeleteExpenseUseCase
import com.kelvinht.moneylife.domain.usecase.GetDailyExpensesUseCase
import com.kelvinht.moneylife.presentation.common.toStyle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getDailyExpenses: GetDailyExpensesUseCase,
    private val calculateDailyBudget: CalculateDailyBudgetUseCase,
    private val deleteExpense: DeleteExpenseUseCase,
    private val dailyLimit: Long
) : ViewModel() {

    private val dateKey = MutableStateFlow(DateUtils.todayKey())

    val uiState: StateFlow<HomeUiState> = dateKey
        .flatMapLatest { key ->
            getDailyExpenses(key).map { expenses ->
                toUiState(calculateDailyBudget(key, expenses, dailyLimit))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HomeUiState(dateLabel = DateUtils.displayDate(dateKey.value))
        )

    /** Re-reads "today" so a new day (or a newly added expense) is reflected. */
    fun refresh() {
        dateKey.value = DateUtils.todayKey()
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch { deleteExpense.invoke(id) }
    }

    private fun toUiState(budget: DailyBudget): HomeUiState {
        val status = when {
            budget.isOverLimit -> BudgetStatus.OVER
            budget.usagePercent >= WARNING_THRESHOLD -> BudgetStatus.WARNING
            else -> BudgetStatus.SAFE
        }

        return HomeUiState(
            dateLabel = DateUtils.displayDate(budget.dateKey),
            spentText = RupiahFormatter.format(budget.spent),
            limitText = "Batas harian: ${RupiahFormatter.format(budget.limit)}",
            remainingText = "Sisa ${RupiahFormatter.format(budget.remaining)}",
            overLimitText = if (budget.isOverLimit) {
                "Lebih ${RupiahFormatter.format(budget.overSpent)} dari batas harian"
            } else {
                null
            },
            usagePercent = budget.usagePercent,
            usageText = budget.usagePercent.toString() + "% Terpakai",
            averageText = RupiahFormatter.format(budget.spent),
            conditionText = when (status) {
                BudgetStatus.SAFE -> "Sangat Aman"
                BudgetStatus.WARNING -> "Perlu Dijaga"
                BudgetStatus.OVER -> "Melebihi Limit"
            },
            makanText = categoryTotal(budget.expenses, ExpenseCategory.MAKAN, "50k"),
            transportText = categoryTotal(budget.expenses, ExpenseCategory.TRANSPORT, "30k"),
            jajanText = categoryTotal(budget.expenses, ExpenseCategory.JAJAN, "20k"),
            budgetStatus = status,
            expenses = budget.expenses.map { it.toUiModel() },
            isEmpty = budget.expenses.isEmpty()
        )
    }

    private fun categoryTotal(
        expenses: List<Expense>,
        category: ExpenseCategory,
        limitLabel: String
    ): String {
        val total = expenses.filter { it.category == category }.sumOf { it.amount }
        val compact = if (total == 0L) "Rp 0" else RupiahFormatter.format(total)
        return "$compact/$limitLabel"
    }

    private fun Expense.toUiModel(): ExpenseUiModel = ExpenseUiModel(
        id = id,
        style = category.toStyle(),
        note = note,
        amountText = RupiahFormatter.format(amount),
        timeText = DateUtils.formatTime(createdAt)
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val WARNING_THRESHOLD = 80
    }
}
