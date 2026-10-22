package com.kelvinht.moneylife.domain.model

import com.kelvinht.moneylife.core.format.RupiahFormatter

object CategoryBudget {
    const val MAKAN_LIMIT: Long = 50_000L
    const val TRANSPORT_LIMIT: Long = 30_000L
    const val JAJAN_LIMIT: Long = 20_000L

    fun limitFor(category: ExpenseCategory): Long = when (category) {
        ExpenseCategory.MAKAN -> MAKAN_LIMIT
        ExpenseCategory.TRANSPORT -> TRANSPORT_LIMIT
        ExpenseCategory.JAJAN -> JAJAN_LIMIT
    }

    fun limitLabelFor(category: ExpenseCategory): String = RupiahFormatter.formatCompact(limitFor(category))
}
