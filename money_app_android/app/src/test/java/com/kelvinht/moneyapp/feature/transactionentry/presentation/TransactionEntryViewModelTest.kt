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
    fun load_returnsReferenceInitialState() {
        val state = viewModel().load()

        assertEquals("Rp 45.000", state.amount)
        assertEquals("Hari ini, 14:20 WIB", state.transactionDate)
        assertTrue(state.categories.first().isSelected)
        assertTrue(state.isSaveEnabled)
    }

    @Test
    fun quickAmountCategoryAndReset_updateState() {
        val viewModel = viewModel()
        assertEquals("Rp 65.000", viewModel.addQuickAmount(20_000).amount)
        val categoryState = viewModel.selectCategory("Jajan")
        assertTrue(categoryState.categories.last().isSelected)
        val resetState = viewModel.reset()
        assertEquals("Rp 0", resetState.amount)
        assertFalse(resetState.isSaveEnabled)
    }

    private fun viewModel() = TransactionEntryViewModel(GetTransactionEntryOptionsUseCase(FakeEntryRepository()))
}

private class FakeEntryRepository : TransactionEntryRepository {
    override fun getOptions() = TransactionEntryOptions(
        categories = listOf("Makan", "Transport", "Jajan"),
        fundingSources = listOf("Tunai"),
        selectedCategory = "Makan",
        selectedSource = "Tunai",
        transactionDate = "Hari ini, 14:20 WIB",
        note = "Makan siang Rendang & Es Teh",
        initialAmount = 45_000
    )
}
