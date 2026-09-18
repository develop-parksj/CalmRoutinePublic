package com.gyoheul.calm_routine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gyoheul.calm_routine.data.LogModel
import com.gyoheul.calm_routine.data.NotificationModel

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            LogModel.d(this, "Device rebooted - rescheduling alarms")

            NotificationModel.setNotificationAlarm(context)
        }
    }
}