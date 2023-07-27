package de.coldtea.smplr.smplralarm.extensions

import java.time.LocalDate


fun LocalDate.isPastDate(date: LocalDate = LocalDate.now()): Boolean {
    return date.isBefore(this) // Check if the date is before today
}

fun LocalDate.isToday(): Boolean {
    val today = LocalDate.now()
    return this.isEqual(today) // Check if this is equal to today
}