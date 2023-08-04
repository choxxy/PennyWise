package de.coldtea.smplr.smplralarm.models

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import java.time.LocalDate

/**
 * Created by [Yasar Naci Gündüz](https://github.com/ColdTea-Projects).
 */
@JsonClass(generateAdapter = true)
@Keep
internal data class AlarmItem(
    val requestId: Int,
    val hour: Int,
    val minute: Int,
    val date: LocalDate,
    val weekDays: List<WeekDays>,
    val isActive: Boolean,
    val infoPairs: String
)