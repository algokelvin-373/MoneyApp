package com.kelvinht.moneylife.presentation.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.usecase.AddExpenseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val addExpense: AddExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    fun selectCategory(category: ExpenseCategory) {
        _uiState.update { it.copy(category = category, error = null) }
    }

    fun onAmountChanged(value: String) {
        val digits = value.filter { it.isDigit() }.take(MAX_AMOUNT_DIGITS)
        _uiState.update { it.copy(amountInput = digits, error = null) }
    }

    fun onNoteChanged(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun selectDate(dateKey: String) {
        if (DateUtils.isTodayOrPast(dateKey)) {
            _uiState.update { it.copy(dateKey = dateKey, error = null) }
        }
    }

    fun addQuickAmount(value: Long) {
        val current = _uiState.value.amountInput.toLongOrNull() ?: 0L
        val updated = (current + value).coerceAtMost(MAX_AMOUNT)
        _uiState.update { it.copy(amountInput = updated.toString(), error = null) }
    }

    fun reset() {
        _uiState.value = AddExpenseUiState(amountInput = "", note = "", dateKey = _uiState.value.dateKey)
    }

    fun save() {
        val amount = _uiState.value.amountInput.toLongOrNull()
        if (amount == null || amount <= 0L) {
            _uiState.update { it.copy(error = "Masukkan nominal lebih dari 0") }
            return
        }

        val snapshot = _uiState.value
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            addExpense(
                category = snapshot.category,
                amount = amount,
                note = snapshot.note,
                dateKey = snapshot.dateKey
            ).onSuccess {
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = throwable.message ?: "Gagal menyimpan pengeluaran"
                    )
                }
            }
        }
    }

    private companion object {
        const val MAX_AMOUNT_DIGITS = 9
        const val MAX_AMOUNT = 999_999_999L
    }
}
