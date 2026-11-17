package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.domain.model.DailyBudget
import com.kelvinht.moneylife.domain.model.Expense

/**
 * Pure domain calculation: turn a list of daily expenses into a [DailyBudget].
 */
class CalculateDailyBudgetUseCase {
    operator fun invoke(
        dateKey: String,
        expenses: List<Expense>,
        limit: Long = DailyBudget.DEFAULT_DAILY_LIMIT,
    ): DailyBudget = DailyBudget(dateKey = dateKey, limit = limit, expenses = expenses)
}
