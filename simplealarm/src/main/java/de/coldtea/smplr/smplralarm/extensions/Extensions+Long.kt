package de.coldtea.smplr.smplralarm.extensions

import java.time.LocalDate


fun Long.asLocalDate(): LocalDate {
    return LocalDate.ofEpochDay(this)
}

