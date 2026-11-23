package com.kelvinht.moneylife.domain.model

object CategoryBudget {
    const val MAKAN_LIMIT: Long = 50_000L
    const val TRANSPORT_LIMIT: Long = 30_000L
    const val JAJAN_LIMIT: Long = 20_000L

    /** Returns a category limit when one is configured. */
    fun limitFor(category: ExpenseCategory): Long? =
        when (category) {
            ExpenseCategory.MAKAN -> MAKAN_LIMIT
            ExpenseCategory.TRANSPORT -> TRANSPORT_LIMIT
            ExpenseCategory.JAJAN -> JAJAN_LIMIT
            ExpenseCategory.KEBUTUHAN_LAIN,
            ExpenseCategory.KEBUTUHAN_ORANG_TUA,
            ExpenseCategory.TOP_UP_COINS_TIKTOK,
            -> null
        }
}
