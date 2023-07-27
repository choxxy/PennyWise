package de.coldtea.smplr.smplralarm.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Created by [Yasar Naci Gündüz](https://github.com/ColdTea-Projects).
 */
@Entity(tableName = "alarm_notification_table")
internal data class AlarmNotificationEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "alarm_notification_id")
    val alarmNotificationId: Int,
    @ColumnInfo(name = "hour")
    val hour: Int,
    @ColumnInfo(name = "min")
    val min: Int,
    @ColumnInfo(name = "alarm_date")
    val date: Long, // LocalDate epochDay value
    @ColumnInfo(name = "week_days")
    val weekDays: String,
    @ColumnInfo(name = "isActive")
    val isActive: Boolean,
    @ColumnInfo(name = "info_pairs")
    val infoPairs: String
)