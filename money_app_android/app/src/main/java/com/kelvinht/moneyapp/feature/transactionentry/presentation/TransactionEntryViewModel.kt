package com.kelvinht.moneyapp.feature.transactionentry.presentation

import androidx.lifecycle.ViewModel
import com.kelvinht.moneyapp.feature.transactionentry.domain.usecase.GetTransactionEntryOptionsUseCase

class TransactionEntryViewModel(getOptions: GetTransactionEntryOptionsUseCase) : ViewModel() {
    private val options = getOptions()
    private var rawAmount = "0"
    private var selectedSource = options.selectedSource

    fun load(): TransactionEntryUiState = buildState()

    fun onKeyPressed(key: String): TransactionEntryUiState {
        rawAmount = when (key) {
            "backspace" -> rawAmount.dropLast(1).ifEmpty { "0" }
            "00" -> if (rawAmount == "0") "0" else rawAmount + key
            else -> if (rawAmount == "0") key else rawAmount + key
        }
        return buildState()
    }

    fun selectSource(source: String): TransactionEntryUiState {
        if (source in options.fundingSources) selectedSource = source
        return buildState()
    }

    private fun buildState() = TransactionEntryUiState(
        amount = rawAmount,
        category = options.selectedCategory,
        datePlaceholder = options.datePlaceholder,
        sources = options.fundingSources.map { FundingSourceUi(it, it == selectedSource) },
        isSaveEnabled = rawAmount != "0"
    )
}
