package com.kelvinht.moneylife.domain

import com.kelvinht.moneylife.domain.model.Expense
import com.kelvinht.moneylife.domain.model.ExpenseCategory
import com.kelvinht.moneylife.domain.usecase.CalculateDailyBudgetUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateDailyBudgetUseCaseTest {
    private val useCase = CalculateDailyBudgetUseCase()

    private fun expense(
        amount: Long,
        category: ExpenseCategory = ExpenseCategory.MAKAN,
    ) = Expense(
        category = category,
        amount = amount,
        dateKey = "2026-10-09",
        createdAt = 0L,
    )

    @Test
    fun `sums spending per category and keeps remaining positive`() {
        val budget =
            useCase(
                dateKey = "2026-10-09",
                expenses =
                    listOf(
                        expense(20_000, ExpenseCategory.MAKAN),
                        expense(15_000, ExpenseCategory.TRANSPORT),
                        expense(5_000, ExpenseCategory.JAJAN),
                    ),
            )

        assertEquals(40_000, budget.spent)
        assertEquals(60_000, budget.remaining)
        assertEquals(40, budget.usagePercent)
        assertFalse(budget.isOverLimit)
        assertEquals(20_000, budget.spentByCategory(ExpenseCategory.MAKAN))
    }

    @Test
    fun `flags over limit and reports the excess`() {
        val budget =
            useCase(
                dateKey = "2026-10-09",
                expenses =
                    listOf(
                        expense(70_000, ExpenseCategory.MAKAN),
                        expense(45_000, ExpenseCategory.JAJAN),
                    ),
            )

        assertTrue(budget.isOverLimit)
        assertEquals(115_000, budget.spent)
        assertEquals(0, budget.remaining)
        assertEquals(15_000, budget.overSpent)
        assertEquals(100, budget.usagePercent)
    }
}
