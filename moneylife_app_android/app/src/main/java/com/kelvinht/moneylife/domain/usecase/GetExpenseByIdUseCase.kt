package com.kelvinht.moneylife.domain.usecase

import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.repository.ExpenseRepository

class GetExpenseByIdUseCase(
    private val repository: ExpenseRepository,
) {
    suspend operator fun invoke(id: Long): Expense? = repository.getExpenseById(id)
}
