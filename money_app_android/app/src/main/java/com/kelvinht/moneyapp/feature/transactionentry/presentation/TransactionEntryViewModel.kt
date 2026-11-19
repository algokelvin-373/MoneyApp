package com.kelvinht.moneyapp.feature.transactionentry.presentation

import androidx.lifecycle.ViewModel
import com.kelvinht.moneyapp.core.format.RupiahFormatter
import com.kelvinht.moneyapp.feature.transactionentry.domain.usecase.GetTransactionEntryOptionsUseCase

class TransactionEntryViewModel(getOptions: GetTransactionEntryOptionsUseCase) : ViewModel() {
    private val options = getOptions()
    private var amount = options.initialAmount
    private var selectedCategory = options.selectedCategory

    fun load(): TransactionEntryUiState = buildState()

    fun addQuickAmount(value: Long): TransactionEntryUiState {
        amount = (amount + value).coerceAtMost(999_999_999)
        return buildState()
    }

    fun selectCategory(category: String): TransactionEntryUiState {
        if (category in options.categories) selectedCategory = category
        return buildState()
    }

    fun reset(): TransactionEntryUiState {
        amount = 0
        selectedCategory = options.selectedCategory
        return buildState()
    }

    private fun buildState() = TransactionEntryUiState(
        amount = RupiahFormatter.format(amount),
        categories = options.categories.map { EntryCategoryUi(it, it == selectedCategory) },
        transactionDate = options.transactionDate,
        source = options.selectedSource,
        note = options.note,
        isSaveEnabled = amount > 0
    )
}
