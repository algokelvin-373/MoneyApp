package com.kelvinht.moneyapp.feature.transactionentry.presentation

import com.kelvinht.moneyapp.feature.transactionentry.domain.model.TransactionEntryOptions
import com.kelvinht.moneyapp.feature.transactionentry.domain.repository.TransactionEntryRepository
import com.kelvinht.moneyapp.feature.transactionentry.domain.usecase.GetTransactionEntryOptionsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionEntryViewModelTest {
    @Test
    fun load_returnsInitialEntryState() {
        val state = viewModel().load()

        assertEquals("0", state.amount)
        assertEquals("Pilih Kategori", state.category)
        assertEquals("mm/dd/yyyy", state.datePlaceholder)
        assertFalse(state.isSaveEnabled)
        assertTrue(state.sources.first().isSelected)
    }

    @Test
    fun keypadAndFundingSource_updateState() {
        val viewModel = viewModel()
        viewModel.onKeyPressed("1")
        viewModel.onKeyPressed("2")
        val amountState = viewModel.onKeyPressed("00")
        assertEquals("1200", amountState.amount)
        assertTrue(amountState.isSaveEnabled)

        val sourceState = viewModel.selectSource("GoPay")
        assertTrue(sourceState.sources.last().isSelected)
        assertFalse(sourceState.sources.first().isSelected)
    }

    private fun viewModel() = TransactionEntryViewModel(GetTransactionEntryOptionsUseCase(FakeEntryRepository()))
}

private class FakeEntryRepository : TransactionEntryRepository {
    override fun getOptions() = TransactionEntryOptions(
        categories = listOf("Makanan"),
        fundingSources = listOf("Tunai", "OVO", "GoPay"),
        selectedCategory = "Pilih Kategori",
        selectedSource = "Tunai",
        datePlaceholder = "mm/dd/yyyy"
    )
}
