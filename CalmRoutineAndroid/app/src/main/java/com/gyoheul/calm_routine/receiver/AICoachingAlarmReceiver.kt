package com.gyoheul.calm_routine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gyoheul.calm_routine.data.AINotificationWorker
import com.gyoheul.calm_routine.data.LogModel
import com.gyoheul.calm_routine.data.NotificationModel

class AICoachingAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        LogModel.d(this, "AI Coaching Alarm triggered!")

        val workRequest = OneTimeWorkRequestBuilder<AINotificationWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)

        NotificationModel.setNotificationAlarm(context)
    }
}
