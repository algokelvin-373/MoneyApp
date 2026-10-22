package com.kelvinht.moneylife.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Date helpers shared across features. Dates are keyed by "yyyy-MM-dd" so they
 * can be stored and queried directly in Room.
 */
object DateUtils {
    private const val DATE_KEY_PATTERN = "yyyy-MM-dd"

    private val dateKeyFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DATE_KEY_PATTERN)
    private val displayFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale("id", "ID"))
    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm", Locale("id", "ID"))

    fun todayKey(): String = LocalDate.now().format(dateKeyFormatter)

    fun parseDate(dateKey: String): LocalDate = LocalDate.parse(dateKey, dateKeyFormatter)

    fun dateKey(date: LocalDate): String = date.format(dateKeyFormatter)

    fun displayDate(dateKey: String): String =
        LocalDate.parse(dateKey, dateKeyFormatter).format(displayFormatter)

    fun formatTime(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(timeFormatter)
}
