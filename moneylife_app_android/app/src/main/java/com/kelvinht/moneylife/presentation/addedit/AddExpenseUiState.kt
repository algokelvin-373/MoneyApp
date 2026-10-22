package com.kelvinht.moneylife.presentation.addedit

import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.core.util.DateUtils

data class AddExpenseUiState(
    val category: ExpenseCategory = ExpenseCategory.MAKAN,
    val amountInput: String = "",
    val note: String = "",
    val dateKey: String = DateUtils.todayKey(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)
