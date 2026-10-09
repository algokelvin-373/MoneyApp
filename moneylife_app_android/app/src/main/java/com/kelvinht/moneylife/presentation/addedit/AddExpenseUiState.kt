package com.kelvinht.moneylife.presentation.addedit

import com.kelvinht.moneylife.domain.model.ExpenseCategory

data class AddExpenseUiState(
    val category: ExpenseCategory = ExpenseCategory.MAKAN,
    val amountInput: String = "",
    val note: String = "",
    val error: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)
