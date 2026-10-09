package com.kelvinht.moneylife.di

import android.content.Context
import com.kelvinht.moneylife.data.local.MoneyLifeDatabase
import com.kelvinht.moneylife.data.repository.ExpenseRepositoryImpl
import com.kelvinht.moneylife.domain.model.DailyBudget
import com.kelvinht.moneylife.domain.repository.ExpenseRepository
import com.kelvinht.moneylife.domain.usecase.AddExpenseUseCase
import com.kelvinht.moneylife.domain.usecase.CalculateDailyBudgetUseCase
import com.kelvinht.moneylife.domain.usecase.DeleteExpenseUseCase
import com.kelvinht.moneylife.domain.usecase.GetDailyExpensesUseCase

/**
 * Simple manual dependency container. Keeps construction of the data layer and
 * use cases in one place without pulling in a DI framework.
 */
class AppContainer(context: Context) {

    private val database: MoneyLifeDatabase = MoneyLifeDatabase.getInstance(context)

    private val expenseRepository: ExpenseRepository = ExpenseRepositoryImpl(database.expenseDao())

    val getDailyExpenses: GetDailyExpensesUseCase = GetDailyExpensesUseCase(expenseRepository)
    val calculateDailyBudget: CalculateDailyBudgetUseCase = CalculateDailyBudgetUseCase()
    val addExpense: AddExpenseUseCase = AddExpenseUseCase(expenseRepository)
    val deleteExpense: DeleteExpenseUseCase = DeleteExpenseUseCase(expenseRepository)

    val dailyLimit: Long = DailyBudget.DEFAULT_DAILY_LIMIT
}
