package com.kelvinht.moneylife.presentation.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinht.moneylife.domain.model.ExpenseCategory
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

    fun save() {
        val amount = _uiState.value.amountInput.toLongOrNull()
        if (amount == null || amount <= 0L) {
            _uiState.update { it.copy(error = "Masukkan nominal lebih dari 0") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            addExpense(
                category = _uiState.value.category,
                amount = amount,
                note = _uiState.value.note
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
    }
}
