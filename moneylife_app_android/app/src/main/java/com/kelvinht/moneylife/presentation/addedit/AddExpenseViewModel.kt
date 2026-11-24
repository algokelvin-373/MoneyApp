package com.kelvinht.moneylife.presentation.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kelvinht.moneylife.R
import com.kelvinht.moneylife.core.util.DateUtils
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.usecase.AddExpenseUseCase
import com.kelvinht.moneylife.domain.usecase.GetExpenseByIdUseCase
import com.kelvinht.moneylife.domain.usecase.UpdateExpenseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddExpenseViewModel(
    private val addExpense: AddExpenseUseCase,
    private val getExpenseById: GetExpenseByIdUseCase,
    private val updateExpense: UpdateExpenseUseCase,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(restoreState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    fun startEditing(id: Long) {
        val state = _uiState.value
        if (id <= 0L || (state.expenseId == id && !state.isLoadingExpense)) return

        updateState { it.copy(expenseId = id, isLoadingExpense = true, error = null) }
        viewModelScope.launch {
            val expense =
                runCatching { getExpenseById(id) }
                    .getOrElse {
                        updateState { state ->
                            state.copy(
                                isLoadingExpense = false,
                                error = R.string.error_load_expense_failed,
                            )
                        }
                        return@launch
                    }
            if (expense == null) {
                updateState {
                    it.copy(
                        isLoadingExpense = false,
                        error = R.string.error_expense_not_found,
                    )
                }
                return@launch
            }

            updateState { state ->
                if (state.expenseId != id) {
                    state
                } else {
                    state.copy(
                        category = expense.category,
                        amountInput = expense.amount.toString(),
                        note = expense.note,
                        dateKey = expense.dateKey,
                        isLoadingExpense = false,
                        error = null,
                    )
                }
            }
        }
    }

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
        val snapshot = _uiState.value
        if (snapshot.isLoadingExpense) return

        val amount = snapshot.amountInput.toLongOrNull()
        if (amount == null || amount <= 0L) {
            updateState { it.copy(error = R.string.error_amount_required) }
            return
        }

        updateState { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val result =
                snapshot.expenseId?.let { id ->
                    updateExpense(
                        id = id,
                        category = snapshot.category,
                        amount = amount,
                        note = snapshot.note,
                    )
                } ?: addExpense(
                    category = snapshot.category,
                    amount = amount,
                    note = snapshot.note,
                    dateKey = snapshot.dateKey,
                ).map { Unit }

            result.onSuccess {
                updateState { it.copy(isSaving = false, isSaved = true) }
            }.onFailure { failure ->
                updateState {
                    it.copy(
                        isSaving = false,
                        error =
                            if (failure is NoSuchElementException) {
                                R.string.error_expense_not_found
                            } else {
                                R.string.error_save_failed
                            },
                    )
                }
            }
        }
    }

    private fun restoreState(): AddExpenseUiState {
        // A saved form can outlive a device clock correction (the clock was
        // ahead and is now behind the saved date). Never restore a date that is
        // no longer valid, otherwise the form opens on a future day.
        val restoredDate = savedStateHandle.get<String>(KEY_DATE).orEmpty()
        return AddExpenseUiState(
            category =
                savedStateHandle.get<String>(KEY_CATEGORY)
                    ?.let { name -> ExpenseCategory.entries.firstOrNull { it.name == name } }
                    ?: ExpenseCategory.MAKAN,
            amountInput = savedStateHandle[KEY_AMOUNT_INPUT] ?: "",
            note = savedStateHandle[KEY_NOTE] ?: "",
            dateKey = restoredDate.takeIf(DateUtils::isTodayOrPast) ?: DateUtils.todayKey(),
            expenseId = savedStateHandle[KEY_EXPENSE_ID],
            isLoadingExpense = savedStateHandle[KEY_LOADING_EXPENSE] ?: false,
        )
    }

    private fun updateState(transform: (AddExpenseUiState) -> AddExpenseUiState) {
        val updated = transform(_uiState.value)
        _uiState.value = updated
        savedStateHandle[KEY_CATEGORY] = updated.category.name
        savedStateHandle[KEY_AMOUNT_INPUT] = updated.amountInput
        savedStateHandle[KEY_NOTE] = updated.note
        savedStateHandle[KEY_DATE] = updated.dateKey
        savedStateHandle[KEY_EXPENSE_ID] = updated.expenseId
        savedStateHandle[KEY_LOADING_EXPENSE] = updated.isLoadingExpense
    }

    private companion object {
        const val MAX_AMOUNT_DIGITS = 9
        const val MAX_AMOUNT = 999_999_999L
        const val KEY_CATEGORY = "category"
        const val KEY_AMOUNT_INPUT = "amount_input"
        const val KEY_NOTE = "note"
        const val KEY_DATE = "date"
        const val KEY_EXPENSE_ID = "expense_id"
        const val KEY_LOADING_EXPENSE = "loading_expense"
    }
}
