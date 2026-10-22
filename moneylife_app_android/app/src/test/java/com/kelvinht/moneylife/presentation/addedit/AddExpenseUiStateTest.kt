package com.kelvinht.moneylife.presentation.addedit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddExpenseUiStateTest {

    @Test
    fun `form opens empty without any dummy transaction`() {
        val state = AddExpenseUiState()

        assertEquals("", state.amountInput)
        assertEquals("", state.note)
        assertTrue(state.error == null)
        assertTrue(!state.isSaving)
        assertTrue(!state.isSaved)
    }
}
