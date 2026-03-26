package com.dadadrive.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// ─────────────────────────────────────────────────────────
// DATE UTILS
// ─────────────────────────────────────────────────────────

object DateUtils {

    private const val PATTERN_DATE = "dd/MM/yyyy"
    private const val PATTERN_DATE_TIME = "dd/MM/yyyy HH:mm"
    private const val PATTERN_TIME = "HH:mm"

    /**
     * Formats a [Date] to a human-readable date string (dd/MM/yyyy).
     */
    fun formatDate(date: Date, locale: Locale = Locale.getDefault()): String =
        SimpleDateFormat(PATTERN_DATE, locale).format(date)

    /**
     * Formats a [Date] to a human-readable date-time string (dd/MM/yyyy HH:mm).
     */
    fun formatDateTime(date: Date, locale: Locale = Locale.getDefault()): String =
        SimpleDateFormat(PATTERN_DATE_TIME, locale).format(date)

    /**
     * Formats a [Date] to a time-only string (HH:mm).
     */
    fun formatTime(date: Date, locale: Locale = Locale.getDefault()): String =
        SimpleDateFormat(PATTERN_TIME, locale).format(date)

    /**
     * Parses a date string with the given [pattern] and returns a [Date],
     * or null if parsing fails.
     */
    fun parse(dateString: String, pattern: String = PATTERN_DATE, locale: Locale = Locale.getDefault()): Date? =
        runCatching { SimpleDateFormat(pattern, locale).parse(dateString) }.getOrNull()

    /**
     * Returns a relative label such as "Today", "Yesterday", or the formatted date.
     * Comparison is based on calendar day boundaries, not elapsed milliseconds.
     */
    fun toRelativeLabel(date: Date, locale: Locale = Locale.getDefault()): String {
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val yesterday = (today.clone() as java.util.Calendar).apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
        val dateCalendar = java.util.Calendar.getInstance().apply { time = date }

        return when {
            isSameDay(dateCalendar, today) -> "Today"
            isSameDay(dateCalendar, yesterday) -> "Yesterday"
            else -> formatDate(date, locale)
        }
    }

    private fun isSameDay(a: java.util.Calendar, b: java.util.Calendar): Boolean =
        a.get(java.util.Calendar.YEAR) == b.get(java.util.Calendar.YEAR) &&
            a.get(java.util.Calendar.DAY_OF_YEAR) == b.get(java.util.Calendar.DAY_OF_YEAR)

    /**
     * Returns a human-readable duration string from a number of seconds (e.g. "2 min 30 s").
     * Negative values are treated as their absolute value.
     */
    fun formatDuration(seconds: Long): String {
        val abs = Math.abs(seconds)
        val min = TimeUnit.SECONDS.toMinutes(abs)
        val sec = abs - TimeUnit.MINUTES.toSeconds(min)
        return when {
            min == 0L -> "${sec}s"
            sec == 0L -> "${min} min"
            else -> "${min} min ${sec}s"
        }
    }
}
