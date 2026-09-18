package com.gyoheul.calm_routine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.data.LogModel
import com.gyoheul.calm_routine.data.NotificationModel
import com.gyoheul.calm_routine.data.SharedPreferencesManager

class NotificationAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        LogModel.d(this, "Alarm triggered!")

        val preferencesManager = SharedPreferencesManager(context)

        // 通知タスクの実行
        val notificationTypes: Set<EnumClass.NotificationType> = preferencesManager.getNotificationTypes()
        notificationTypes.forEach { type ->
            NotificationModel.showNotification(
                context,
                when (type) {
                    EnumClass.NotificationType.Routine -> NotificationModel.getRoutineNotificationData(context)
                    EnumClass.NotificationType.Mood -> NotificationModel.getMoodNotificationData(context)
                    EnumClass.NotificationType.AI -> null
                },
            )
        }
        NotificationModel.setNotificationAlarm(context)
    }
}
