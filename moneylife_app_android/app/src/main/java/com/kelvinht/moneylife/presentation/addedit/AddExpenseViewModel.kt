package com.kelvinht.moneylife.presentation.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinht.moneylife.R
import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.usecase.AddExpenseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val addExpense: AddExpenseUseCase,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(restoreState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    fun selectCategory(category: ExpenseCategory) {
        updateState { it.copy(category = category, error = null) }
    }

    fun onAmountChanged(value: String) {
        val digits = value.filter { it.isDigit() }.take(MAX_AMOUNT_DIGITS)
        updateState { it.copy(amountInput = digits, error = null) }
    }

    fun onNoteChanged(value: String) {
        updateState { it.copy(note = value) }
    }

    fun selectDate(dateKey: String) {
        if (DateUtils.isTodayOrPast(dateKey)) {
            updateState { it.copy(dateKey = dateKey, error = null) }
        }
    }

    fun addQuickAmount(value: Long) {
        val current = _uiState.value.amountInput.toLongOrNull() ?: 0L
        val updated = (current + value).coerceAtMost(MAX_AMOUNT)
        updateState { it.copy(amountInput = updated.toString(), error = null) }
    }

    fun reset() {
        updateState {
            it.copy(
                amountInput = "",
                note = "",
                error = null,
                isSaving = false,
                isSaved = false,
            )
        }
    }

    fun save() {
        val amount = _uiState.value.amountInput.toLongOrNull()
        if (amount == null || amount <= 0L) {
            updateState { it.copy(error = R.string.error_amount_required) }
            return
        }

        val snapshot = _uiState.value
        updateState { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            addExpense(
                category = snapshot.category,
                amount = amount,
                note = snapshot.note,
                dateKey = snapshot.dateKey,
            ).onSuccess {
                updateState { it.copy(isSaving = false, isSaved = true) }
            }.onFailure {
                updateState {
                    it.copy(
                        isSaving = false,
                        error = R.string.error_save_failed,
                    )
                }
            }
        }
    }

    private fun restoreState(): AddExpenseUiState =
        AddExpenseUiState(
            category =
                savedStateHandle.get<String>(KEY_CATEGORY)
                    ?.let { name -> ExpenseCategory.entries.firstOrNull { it.name == name } }
                    ?: ExpenseCategory.MAKAN,
            amountInput = savedStateHandle[KEY_AMOUNT_INPUT] ?: "",
            note = savedStateHandle[KEY_NOTE] ?: "",
            dateKey = savedStateHandle[KEY_DATE] ?: DateUtils.todayKey(),
        )

    private fun updateState(transform: (AddExpenseUiState) -> AddExpenseUiState) {
        val updated = transform(_uiState.value)
        _uiState.value = updated
        savedStateHandle[KEY_CATEGORY] = updated.category.name
        savedStateHandle[KEY_AMOUNT_INPUT] = updated.amountInput
        savedStateHandle[KEY_NOTE] = updated.note
        savedStateHandle[KEY_DATE] = updated.dateKey
    }

    private companion object {
        const val MAX_AMOUNT_DIGITS = 9
        const val MAX_AMOUNT = 999_999_999L
        const val KEY_CATEGORY = "category"
        const val KEY_AMOUNT_INPUT = "amount_input"
        const val KEY_NOTE = "note"
        const val KEY_DATE = "date"
    }
}
