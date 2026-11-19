package com.kelvinht.moneylife.di

import android.content.Context
import com.kelvinht.moneylife.data.local.MoneyLifeDatabase
import com.kelvinht.moneylife.data.repository.DailyLimitRepositoryImpl
import com.kelvinht.moneylife.data.repository.ExpenseRepositoryImpl
import com.kelvinht.moneylife.data.repository.RoomTransactionRunner
import com.kelvinht.moneylife.data.repository.SavingsRepositoryImpl
import com.kelvinht.moneylife.domain.repository.DailyLimitProvider
import com.kelvinht.moneylife.domain.repository.DailyLimitRepository
import com.kelvinht.moneylife.domain.repository.ExpenseRepository
import com.kelvinht.moneylife.domain.repository.SavingsRepository
import com.kelvinht.moneylife.domain.repository.TransactionRunner
import com.kelvinht.moneylife.domain.usecase.AddExpenseUseCase
import com.kelvinht.moneylife.domain.usecase.CalculateDailyBudgetUseCase
import com.kelvinht.moneylife.domain.usecase.DeleteExpenseUseCase
import com.kelvinht.moneylife.domain.usecase.GetDailyExpensesUseCase
import com.kelvinht.moneylife.domain.usecase.GetDailyLimitUseCase
import com.kelvinht.moneylife.domain.usecase.ObserveDailyLimitUseCase
import com.kelvinht.moneylife.domain.usecase.ObserveSavingsBalanceUseCase
import com.kelvinht.moneylife.domain.usecase.RolloverDailySavingsUseCase
import com.kelvinht.moneylife.domain.usecase.SetDailyLimitUseCase

/**
 * Simple manual dependency container. Keeps construction of the data layer and
 * use cases in one place without pulling in a DI framework.
 */
class AppContainer(context: Context) {
    private val database: MoneyLifeDatabase = MoneyLifeDatabase.getInstance(context)

    private val expenseRepository: ExpenseRepository = ExpenseRepositoryImpl(database.expenseDao())
    private val savingsRepository: SavingsRepository = SavingsRepositoryImpl(database.savingsDao())
    private val transactionRunner: TransactionRunner = RoomTransactionRunner(database)
    private val dailyLimitRepository: DailyLimitRepository =
        DailyLimitRepositoryImpl(database.savingsDao())

    val getDailyLimit: GetDailyLimitUseCase = GetDailyLimitUseCase(dailyLimitRepository)
    val observeDailyLimit: ObserveDailyLimitUseCase =
        ObserveDailyLimitUseCase(dailyLimitRepository)
    val setDailyLimit: SetDailyLimitUseCase = SetDailyLimitUseCase(dailyLimitRepository)

    private val dailyLimitProvider = DailyLimitProvider { getDailyLimit() }

    val getDailyExpenses: GetDailyExpensesUseCase = GetDailyExpensesUseCase(expenseRepository)
    val calculateDailyBudget: CalculateDailyBudgetUseCase = CalculateDailyBudgetUseCase()
    val addExpense: AddExpenseUseCase =
        AddExpenseUseCase(expenseRepository, savingsRepository, transactionRunner, dailyLimitProvider)
    val deleteExpense: DeleteExpenseUseCase =
        DeleteExpenseUseCase(
            expenseRepository,
            savingsRepository,
            transactionRunner,
            dailyLimitProvider,
        )
    val observeSavingsBalance = ObserveSavingsBalanceUseCase(savingsRepository)
    val rolloverDailySavings =
        RolloverDailySavingsUseCase(expenseRepository, savingsRepository, transactionRunner)
}
