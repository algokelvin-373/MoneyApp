package com.kelvinht.moneylife.domain

import com.kelvinht.moneylife.domain.model.CategoryBudget
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryBudgetTest {

    @Test
    fun `returns the configured limit for every category`() {
        assertEquals(50_000L, CategoryBudget.limitFor(ExpenseCategory.MAKAN))
        assertEquals(30_000L, CategoryBudget.limitFor(ExpenseCategory.TRANSPORT))
        assertEquals(20_000L, CategoryBudget.limitFor(ExpenseCategory.JAJAN))
    }

    @Test
    fun `formats the category limit label compactly`() {
        assertEquals("50k", CategoryBudget.limitLabelFor(ExpenseCategory.MAKAN))
        assertEquals("30k", CategoryBudget.limitLabelFor(ExpenseCategory.TRANSPORT))
        assertEquals("20k", CategoryBudget.limitLabelFor(ExpenseCategory.JAJAN))
    }
}
