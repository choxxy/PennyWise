package de.coldtea.smplr.smplralarm.models

import android.content.Intent
import androidx.annotation.Keep
import de.coldtea.smplr.smplralarm.extensions.activeDaysAsJsonString
import de.coldtea.smplr.smplralarm.repository.entity.AlarmNotificationEntity
import de.coldtea.smplr.smplralarm.repository.entity.NotificationChannelEntity
import de.coldtea.smplr.smplralarm.repository.entity.NotificationEntity
import java.time.LocalDate

/**
 * Created by [Yasar Naci Gündüz](https://github.com/ColdTea-Projects).
 */

internal class SmplrAlarmReceiverObjects {
    companion object {
        internal const val SMPLR_ALARM_RECEIVER_INTENT_ID = "smplr_alarm_receiver_intent_id"

        internal var alarmNotification: MutableList<AlarmNotification> = mutableListOf()
    }
}
@Keep
data class AlarmNotification(
    val alarmNotificationId: Int,
    val hour: Int,
    val min: Int,
    val date: LocalDate,
    val weekDays: List<WeekDays>,
    val notificationChannelItem: NotificationChannelItem?,
    val notificationItem: NotificationItem?,
    val contentIntent: Intent?,
    val fullScreenIntent: Intent?,
    val alarmReceivedIntent: Intent?,
    val isActive: Boolean,
    val infoPairs: String
)

internal fun AlarmNotification.extractAlarmNotificationEntity(): AlarmNotificationEntity =
    AlarmNotificationEntity(
        alarmNotificationId,
        hour,
        min,
        date.toEpochDay(),
        weekDays.activeDaysAsJsonString(),
        isActive,
        infoPairs
    )

internal fun AlarmNotification.extractNotificationEntity(fkAlarmNotificationId: Int): NotificationEntity =
    NotificationEntity(
        0,
        fkAlarmNotificationId,
        notificationItem?.smallIcon?:0,
        notificationItem?.title.orEmpty(),
        notificationItem?.message.orEmpty(),
        notificationItem?.bigText.orEmpty(),
        notificationItem?.autoCancel?:false,
        notificationItem?.firstButtonText.orEmpty(),
        notificationItem?.secondButtonText.orEmpty()
    )

internal fun AlarmNotification.extractNotificationChannelEntity(fkAlarmNotificationId: Int): NotificationChannelEntity =
    NotificationChannelEntity(
        0,
        fkAlarmNotificationId,
        notificationChannelItem?.importance?:0,
        notificationChannelItem?.showBadge?:false,
        notificationChannelItem?.name.orEmpty(),
        notificationChannelItem?.description.orEmpty()
    )