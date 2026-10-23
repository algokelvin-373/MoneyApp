package com.kelvinht.moneylife.presentation.addedit

import androidx.annotation.StringRes
import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.model.ExpenseCategory

data class AddExpenseUiState(
    val category: ExpenseCategory = ExpenseCategory.MAKAN,
    val amountInput: String = "",
    val note: String = "",
    val dateKey: String = DateUtils.todayKey(),
    @StringRes val error: Int? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)
