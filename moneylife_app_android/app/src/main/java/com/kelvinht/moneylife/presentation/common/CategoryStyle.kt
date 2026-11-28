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
    @ColorRes val colorRes: Int,
) {
    MAKAN(R.string.category_makan, "\uD83C\uDF5A", R.color.category_makan),
    TRANSPORT(R.string.category_transport, "\uD83D\uDE8C", R.color.category_transport),
    JAJAN(R.string.category_jajan, "\uD83C\uDF6C", R.color.category_jajan),
    KEBUTUHAN_LAIN(R.string.category_kebutuhan_lain, "\uD83E\uDDFE", R.color.category_other),
    KEBUTUHAN_ORANG_TUA(R.string.category_kebutuhan_orang_tua, "\uD83E\uDEF6", R.color.category_parents),
    TOP_UP_COINS_TIKTOK(R.string.category_top_up_coins_tiktok, "\uD83E\uDE99", R.color.category_tiktok),
    UANG_AMAL(R.string.category_uang_amal, "\uD83D\uDC9D", R.color.category_amal),
}

fun ExpenseCategory.toStyle(): CategoryStyle =
    when (this) {
        ExpenseCategory.MAKAN -> CategoryStyle.MAKAN
        ExpenseCategory.TRANSPORT -> CategoryStyle.TRANSPORT
        ExpenseCategory.JAJAN -> CategoryStyle.JAJAN
        ExpenseCategory.KEBUTUHAN_LAIN -> CategoryStyle.KEBUTUHAN_LAIN
        ExpenseCategory.KEBUTUHAN_ORANG_TUA -> CategoryStyle.KEBUTUHAN_ORANG_TUA
        ExpenseCategory.TOP_UP_COINS_TIKTOK -> CategoryStyle.TOP_UP_COINS_TIKTOK
        ExpenseCategory.UANG_AMAL -> CategoryStyle.UANG_AMAL
    }
