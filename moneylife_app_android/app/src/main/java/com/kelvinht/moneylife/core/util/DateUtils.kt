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
    private val shortDisplayFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID"))
    private val monthDisplayFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM yyyy", Locale("id", "ID"))
    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("HH:mm", Locale("id", "ID"))

    fun todayKey(): String = LocalDate.now().format(dateKeyFormatter)

    /** True when [dateKey] is today or any earlier day. */
    fun isTodayOrPast(dateKey: String): Boolean =
        !parseDate(dateKey).isAfter(LocalDate.now())

    /** True only for days before today; today itself is not final yet. */
    fun isPast(dateKey: String): Boolean =
        parseDate(dateKey).isBefore(LocalDate.now())

    fun parseDate(dateKey: String): LocalDate = LocalDate.parse(dateKey, dateKeyFormatter)

    fun dateKey(date: LocalDate): String = date.format(dateKeyFormatter)

    /** Extracts the "yyyy-MM" month key from a "yyyy-MM-dd" date key. */
    fun monthKeyOf(dateKey: String): String = dateKey.substring(0, 7)

    fun displayDate(dateKey: String): String =
        LocalDate.parse(dateKey, dateKeyFormatter).format(displayFormatter)

    /** Example: "2026-10-09" -> "9 Okt 2026" */
    fun shortDisplayDate(dateKey: String): String =
        LocalDate.parse(dateKey, dateKeyFormatter).format(shortDisplayFormatter)

    /** Example: "2026-10-09" -> "Okt 2026" */
    fun monthLabel(dateKey: String): String =
        LocalDate.parse(dateKey, dateKeyFormatter).format(monthDisplayFormatter)

    fun formatTime(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(timeFormatter)
}
