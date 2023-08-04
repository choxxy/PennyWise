package de.coldtea.smplr.smplralarm.models

import androidx.annotation.Keep

/**
 * Created by [Yasar Naci Gündüz](https://github.com/ColdTea-Projects).
 */
@Keep
enum class WeekDays(val value: Int) {
    SUNDAY(1),
    MONDAY(2),
    TUESDAY(3),
    WEDNESDAY(4),
    THURSDAY(5),
    FRIDAY(6),
    SATURDAY(7)
}