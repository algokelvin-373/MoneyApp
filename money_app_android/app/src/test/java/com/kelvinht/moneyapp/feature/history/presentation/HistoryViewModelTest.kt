package com.kelvinht.moneyapp.feature.history.presentation

import com.kelvinht.moneyapp.feature.history.domain.model.TransactionEntryOptions
import com.kelvinht.moneyapp.feature.history.domain.repository.HistoryRepository
import com.kelvinht.moneyapp.feature.history.domain.usecase.GetTransactionEntryOptionsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryViewModelTest {
    @Test
    fun loadHistory_returnsInitialTransactionEntryState() {
        val viewModel = HistoryViewModel(GetTransactionEntryOptionsUseCase(FakeHistoryRepository()))

        val state = viewModel.loadHistory()

        assertEquals("0", state.amount)
        assertEquals("Pilih Kategori", state.category)
        assertEquals("mm/dd/yyyy", state.datePlaceholder)
        assertFalse(state.isSaveEnabled)
        assertTrue(state.sources[0].isSelected)
    }

    @Test
    fun keypad_updatesAmountAndSaveState() {
        val viewModel = HistoryViewModel(GetTransactionEntryOptionsUseCase(FakeHistoryRepository()))

        viewModel.onKeyPressed("1")
        viewModel.onKeyPressed("2")
        val state = viewModel.onKeyPressed("00")

        assertEquals("1200", state.amount)
        assertTrue(state.isSaveEnabled)

        val afterDelete = viewModel.onKeyPressed("backspace")
        assertEquals("120", afterDelete.amount)
    }

    @Test
    fun selectSource_marksOnlySelectedFundingSource() {
        val viewModel = HistoryViewModel(GetTransactionEntryOptionsUseCase(FakeHistoryRepository()))

        val state = viewModel.selectSource("GoPay")

        assertFalse(state.sources[0].isSelected)
        assertFalse(state.sources[1].isSelected)
        assertTrue(state.sources[2].isSelected)
    }
}

private class FakeHistoryRepository : HistoryRepository {
    override fun getTransactionEntryOptions(): TransactionEntryOptions {
        return TransactionEntryOptions(
            categories = listOf("Makanan", "Transportasi"),
            fundingSources = listOf("Tunai", "OVO", "GoPay"),
            selectedCategory = "Pilih Kategori",
            selectedSource = "Tunai",
            datePlaceholder = "mm/dd/yyyy"
        )
    }
}
