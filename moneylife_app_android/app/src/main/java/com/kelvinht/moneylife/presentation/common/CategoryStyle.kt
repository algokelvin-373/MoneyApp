package com.kelvinht.moneylife.presentation.common

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.kelvinht.moneylife.R
import com.kelvinht.moneylife.domain.model.ExpenseCategory

/**
 * Presentation-only description of a category (label, emoji and accent color).
 * The domain enum stays free of Android resources.
 */
enum class CategoryStyle(
    @StringRes val labelRes: Int,
    val emoji: String,
    @ColorRes val colorRes: Int
) {
    MAKAN(R.string.category_makan, "\uD83C\uDF5A", R.color.category_makan),
    TRANSPORT(R.string.category_transport, "\uD83D\uDE8C", R.color.category_transport),
    JAJAN(R.string.category_jajan, "\uD83C\uDF6C", R.color.category_jajan)
}

fun ExpenseCategory.toStyle(): CategoryStyle = when (this) {
    ExpenseCategory.MAKAN -> CategoryStyle.MAKAN
    ExpenseCategory.TRANSPORT -> CategoryStyle.TRANSPORT
    ExpenseCategory.JAJAN -> CategoryStyle.JAJAN
}
