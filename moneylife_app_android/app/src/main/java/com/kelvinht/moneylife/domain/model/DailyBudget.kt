package com.kelvinht.moneylife.domain.model

/**
 * Aggregated spending for one day together with the daily limit and all the
 * derived values the UI needs.
 */
data class DailyBudget(
    val dateKey: String,
    val limit: Long,
    val expenses: List<Expense>,
) {
    val spent: Long = expenses.sumOf { it.amount }

    val remaining: Long = (limit - spent).coerceAtLeast(0L)

    val overSpent: Long = (spent - limit).coerceAtLeast(0L)

    val isOverLimit: Boolean get() = spent > limit

    /** 0..100, capped so the progress bar never overflows. */
    val usagePercent: Int
        get() = if (limit <= 0L) 0 else ((spent * 100) / limit).toInt().coerceIn(0, 100)

    fun spentByCategory(category: ExpenseCategory): Long = expenses.filter { it.category == category }.sumOf { it.amount }

    companion object {
        const val DAILY_LIMIT_50K: Long = 50_000L
        const val DAILY_LIMIT_100K: Long = 100_000L
        const val DEFAULT_DAILY_LIMIT: Long = DAILY_LIMIT_100K

        /** The daily limits the user can choose from. */
        val DAILY_LIMIT_OPTIONS: List<Long> = listOf(DAILY_LIMIT_50K, DAILY_LIMIT_100K)

        fun isSupportedLimit(value: Long): Boolean = value in DAILY_LIMIT_OPTIONS
    }
}
