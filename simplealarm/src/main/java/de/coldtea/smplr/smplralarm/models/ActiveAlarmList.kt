package de.coldtea.smplr.smplralarm.models

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

/**
 * Created by [Yasar Naci Gündüz](https://github.com/ColdTea-Projects).
 */
@JsonClass(generateAdapter = true)
@Keep
internal data class ActiveAlarmList(
    val alarmItems: List<AlarmItem>
)