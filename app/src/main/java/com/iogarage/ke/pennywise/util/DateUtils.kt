package com.iogarage.ke.pennywise.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale


private const val SECOND_MILLIS = 1000
private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
private const val DAY_MILLIS = 24 * HOUR_MILLIS


fun Long.asDateString(
    pattern: String = "E, dd MMM, yyyy",
    locale: Locale = Locale.getDefault()
): String {
    val localDate = LocalDate.ofEpochDay(this)
    val formatter = DateTimeFormatter.ofPattern(pattern).withLocale(locale)
    return localDate.format(formatter)
}


fun LocalDate.asString(
    pattern: String = "E, dd MMM, yyyy",
    locale: Locale = Locale.getDefault()
): String {
    val formatter = DateTimeFormatter.ofPattern(pattern).withLocale(locale)
    return this.format(formatter)
}

fun LocalDateTime.asString(
    pattern: String = "E, dd MMM, yyyy HH:mm",
    locale: Locale = Locale.getDefault()
): String {
    val formatter = DateTimeFormatter.ofPattern(pattern).withLocale(locale)
    return this.format(formatter)
}

fun String.toLocalDate(
    pattern: String = "MMM dd",
    locale: Locale = Locale.getDefault()
): LocalDate {
    val formatter = DateTimeFormatter.ofPattern(pattern).withLocale(locale)
    return LocalDate.parse(this, formatter)
}


fun LocalTime.toSimpleString(
    pattern: String = "HH:mm",
    locale: Locale = Locale.getDefault()
): String {
    val formatter = DateTimeFormatter.ofPattern(pattern).withLocale(locale)
    return this.format(formatter)
}


fun String.toLocalTime(
    pattern: String = "HH:mm",
    locale: Locale = Locale.getDefault()
): LocalDate {
    val formatter = DateTimeFormatter.ofPattern(pattern).withLocale(locale)
    return LocalDate.parse(this, formatter)
}


fun getDays(year: Int, month: Int): List<Int> {
    val yearMonth: YearMonth = YearMonth.of(year, month)
    val daysInMonth = yearMonth.lengthOfMonth()
    return (1..daysInMonth).map { it }
}
