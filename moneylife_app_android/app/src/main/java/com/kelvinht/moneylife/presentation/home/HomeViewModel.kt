package com.kelvinht.moneylife.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.model.DailyBudget
import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.usecase.CalculateDailyBudgetUseCase
import com.kelvinht.moneylife.domain.usecase.DeleteExpenseUseCase
import com.kelvinht.moneylife.domain.usecase.GetDailyExpensesUseCase
import com.kelvinht.moneylife.domain.usecase.ObserveSavingsBalanceUseCase
import com.kelvinht.moneylife.domain.usecase.RolloverDailySavingsUseCase
import com.kelvinht.moneylife.presentation.common.toStyle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getDailyExpenses: GetDailyExpensesUseCase,
    private val calculateDailyBudget: CalculateDailyBudgetUseCase,
    private val deleteExpense: DeleteExpenseUseCase,
    private val observeSavingsBalance: ObserveSavingsBalanceUseCase,
    private val rolloverDailySavings: RolloverDailySavingsUseCase,
    private val dailyLimit: Long
) : ViewModel() {

    private val dateKey = MutableStateFlow(DateUtils.todayKey())
    private var lastTodayKey = dateKey.value
    val selectedDateKey: String get() = dateKey.value

    val uiState: StateFlow<HomeUiState> = dateKey
        .flatMapLatest { key ->
            combine(
                getDailyExpenses(key),
                observeSavingsBalance(DateUtils.monthKeyOf(key))
            ) { expenses, savingsBalance ->
                toUiState(
                    budget = calculateDailyBudget(key, expenses, dailyLimit),
                    savingsBalance = savingsBalance
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HomeUiState(dateKey = dateKey.value)
        )

    /** Advance the selected day only when the user was viewing today. */
    fun refresh() {
        val today = DateUtils.todayKey()
        viewModelScope.launch {
            rolloverDailySavings(today, dailyLimit)
            if (dateKey.value == lastTodayKey) dateKey.value = today
            lastTodayKey = today
        }
    }

    fun selectDate(key: String) {
        if (DateUtils.isTodayOrPast(key)) dateKey.value = key
    }

    fun previousDay() {
        dateKey.value = DateUtils.dateKey(DateUtils.parseDate(dateKey.value).minusDays(1))
    }

    fun nextDay() {
        val next = DateUtils.dateKey(DateUtils.parseDate(dateKey.value).plusDays(1))
        selectDate(next)
    }

    fun showToday() {
        dateKey.value = DateUtils.todayKey()
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch { deleteExpense.invoke(id) }
    }

    private fun toUiState(budget: DailyBudget, savingsBalance: Long): HomeUiState {
        val status = when {
            budget.isOverLimit -> BudgetStatus.OVER
            budget.usagePercent >= WARNING_THRESHOLD -> BudgetStatus.WARNING
            else -> BudgetStatus.SAFE
        }

        return HomeUiState(
            dateKey = budget.dateKey,
            spent = budget.spent,
            limit = budget.limit,
            remaining = budget.remaining,
            overSpent = budget.overSpent,
            usagePercent = budget.usagePercent,
            savingsBalance = savingsBalance,
            budgetStatus = status,
            spentByCategory = ExpenseCategory.entries.associateWith { budget.spentByCategory(it) },
            expenses = budget.expenses.map { it.toUiModel() },
            isEmpty = budget.expenses.isEmpty()
        )
    }

    private fun Expense.toUiModel(): ExpenseUiModel = ExpenseUiModel(
        id = id,
        style = category.toStyle(),
        note = note,
        amount = amount,
        createdAt = createdAt
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val WARNING_THRESHOLD = 80
    }
}
