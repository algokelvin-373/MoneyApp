package com.kelvinht.moneylife.domain

import com.kelvinht.moneylife.domain.model.CategoryBudget
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CategoryBudgetTest {
    @Test
    fun `returns the configured limit for every category`() {
        assertEquals(50_000L, CategoryBudget.limitFor(ExpenseCategory.MAKAN))
        assertEquals(30_000L, CategoryBudget.limitFor(ExpenseCategory.TRANSPORT))
        assertEquals(20_000L, CategoryBudget.limitFor(ExpenseCategory.JAJAN))
        assertNull(CategoryBudget.limitFor(ExpenseCategory.KEBUTUHAN_LAIN))
        assertNull(CategoryBudget.limitFor(ExpenseCategory.KEBUTUHAN_ORANG_TUA))
        assertNull(CategoryBudget.limitFor(ExpenseCategory.TOP_UP_COINS_TIKTOK))
    }
}
