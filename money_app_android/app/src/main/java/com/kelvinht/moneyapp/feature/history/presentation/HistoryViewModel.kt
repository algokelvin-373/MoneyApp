package com.kelvinht.moneyapp.feature.history.presentation

import androidx.lifecycle.ViewModel
import com.kelvinht.moneyapp.feature.history.domain.usecase.GetTransactionEntryOptionsUseCase

class HistoryViewModel(
    private val getOptions: GetTransactionEntryOptionsUseCase
) : ViewModel() {
    private val options = getOptions()
    private var rawAmount = "0"
    private var selectedSource = options.selectedSource

    fun loadHistory(): HistoryUiState = buildState()

    fun onKeyPressed(key: String): HistoryUiState {
        rawAmount = when (key) {
            "backspace" -> rawAmount.dropLast(1).ifEmpty { "0" }
            "00" -> if (rawAmount == "0") "0" else rawAmount + key
            else -> {
                if (rawAmount == "0") key else rawAmount + key
            }
        }
        return buildState()
    }

    fun selectSource(source: String): HistoryUiState {
        if (source in options.fundingSources) {
            selectedSource = source
        }
        return buildState()
    }

    private fun buildState(): HistoryUiState {
        return HistoryUiState(
            amount = rawAmount,
            category = options.selectedCategory,
            datePlaceholder = options.datePlaceholder,
            sources = options.fundingSources.map {
                FundingSourceUi(name = it, isSelected = it == selectedSource)
            },
            isSaveEnabled = rawAmount != "0"
        )
    }
}
