package com.gyoheul.calm_routine.data

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gyoheul.calm_routine.R
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.common.Extension.channelId
import com.gyoheul.calm_routine.receiver.AICoachingAlarmReceiver
import com.gyoheul.calm_routine.receiver.NotificationAlarmReceiver
import org.json.JSONObject
import java.time.Duration
import java.time.LocalTime
import java.util.Calendar

object NotificationModel {
    data class NotificationData(
        val channelId: String,
        @DrawableRes val smallIcon: Int,
        val title: String,
        val message: String,
        val notificationId: Int
    )

    fun setNotificationAlarm(context: Context) {
        val preferencesManager = SharedPreferencesManager(context)
        val notificationTypes = preferencesManager.getNotificationTypes()
        if (notificationTypes.isNotEmpty()) {
            val time = preferencesManager.getNotificationTime(LocalTime.of(8, 0))
            if (notificationTypes.contains(EnumClass.NotificationType.AI)) {
                scheduleAICoachingAlarms(
                    context,
                    time,
                    preferencesManager.getAICommentFrequency(EnumClass.AICommentFrequency.Low)
                )
            }
            if (notificationTypes.contains(EnumClass.NotificationType.Mood) || notificationTypes.contains(EnumClass.NotificationType.Routine)) {
                scheduleDailyAlarm(context, time)
            }
        } else {
            cancelDailyAlarm(context)
            cancelAICoachingAlarms(context)
        }
    }

    private fun scheduleDailyAlarm(context: Context, time: LocalTime) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.SCHEDULE_EXACT_ALARM
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val now = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 1)
        }
        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, time.hour)
            set(Calendar.MINUTE, time.minute)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DATE, 1) // 時間が既に過ぎていれば翌日に予約
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                LogModel.d(this, "Alarm scheduled at: ${scheduledTime.time}")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTime.timeInMillis,
                    getAlarmIntent(context)
                )
            }
        } else {
            LogModel.d(this, "Alarm scheduled at: ${scheduledTime.time}")
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                scheduledTime.timeInMillis,
                getAlarmIntent(context)
            )
        }
    }

    fun cancelDailyAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(getAlarmIntent(context))
    }

    private fun getAlarmIntent(context: Context): PendingIntent {
        val intent = Intent(context, NotificationAlarmReceiver::class.java).apply {
            action = "com.gyoheul.calm_routine.ACTION_DAILY_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent
    }

    fun scheduleAICoachingAlarms(
        context: Context,
        baseTime: LocalTime,
        frequency: EnumClass.AICommentFrequency
    ) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.SCHEDULE_EXACT_ALARM
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        cancelAICoachingAlarms(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val startTime = LocalTime.of(8, 0)
        val endTime = LocalTime.of(20, 0)

        val times: List<LocalTime> = when (frequency) {
            EnumClass.AICommentFrequency.Low -> listOf(baseTime)
            EnumClass.AICommentFrequency.Medium -> {
                val other = if (baseTime == startTime || baseTime == endTime) {
                    // 端点なら反対側
                    if (baseTime == startTime) endTime else startTime
                } else {
                    // baseTimeとstart~endの間の時間を中間時間に選択
                    val durationToStart = Duration.between(startTime, baseTime).abs()
                    val durationToEnd = Duration.between(baseTime, endTime).abs()
                    if (durationToStart < durationToEnd) endTime else startTime
                }
                listOf(baseTime, other).sorted()
            }
            EnumClass.AICommentFrequency.High -> {
                val fixed = listOf(startTime, endTime)
                val middle1 = startTime.plusHours(4)  // 12:00
                val middle2 = startTime.plusHours(8)  // 16:00
                val basePlus = listOf(baseTime)
                val candidates = (fixed + middle1 + middle2)
                    .filterNot { it == baseTime }
                    .sortedBy { Duration.between(baseTime, it).abs() }
                    .take(3)

                (basePlus + candidates).sorted()
            }
        }

        val now = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 1)
        }
        times.forEachIndexed { index, time ->
            val alarmCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, time.hour)
                set(Calendar.MINUTE, time.minute)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DATE, 1)
            }
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmCalendar.timeInMillis,
                getAICoachingAlarmIntent(context, index + 1)
            )
            LogModel.d(this, "AI Coaching Alarm #${index + 1} scheduled at: ${alarmCalendar.time}")
        }
    }

    fun cancelAICoachingAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 基本および追加の通知をすべてキャンセル
        for (requestCode in 0..3) {
            alarmManager.cancel(getAICoachingAlarmIntent(context, requestCode))
        }
    }

    private fun getAICoachingAlarmIntent(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, AICoachingAlarmReceiver::class.java).apply {
            action = "com.gyoheul.calm_routine.ACTION_AI_COACHING_ALARM"
            putExtra("ALARM_ID", requestCode)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showNotification(
        context: Context,
        data: NotificationData?,
    ) {
        if (data == null) return

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        data.run {
            val shortMessage =
                if (message.length > 40) {
                    message.take(40) + "... \uD83D\uDCAC"
                } else {
                    message
                }
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setContentText(shortMessage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .build()

            LogModel.d(this, "Sending notification...")

            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    fun getRoutineNotificationData(context: Context): NotificationData {
        val dataList =
            listOf(
                R.string.notification_title_routine_01 to R.string.notification_msg_routine_01,
                R.string.notification_title_routine_02 to R.string.notification_msg_routine_02,
                R.string.notification_title_routine_03 to R.string.notification_msg_routine_03,
                R.string.notification_title_routine_04 to R.string.notification_msg_routine_04,
                R.string.notification_title_routine_05 to R.string.notification_msg_routine_05,
                R.string.notification_title_routine_06 to R.string.notification_msg_routine_06,
                R.string.notification_title_routine_07 to R.string.notification_msg_routine_07,
                R.string.notification_title_routine_08 to R.string.notification_msg_routine_08,
                R.string.notification_title_routine_09 to R.string.notification_msg_routine_09,
                R.string.notification_title_routine_10 to R.string.notification_msg_routine_10,
            )
        val (titleId, messageId) = dataList.random()
        return NotificationData(
            EnumClass.NotificationType.Routine.channelId,
            R.drawable.ic_notification_routine,
            context.getString(titleId),
            context.getString(messageId),
            1001
        )
    }

    fun getMoodNotificationData(context: Context): NotificationData {
        val dataList =
            listOf(
                R.string.notification_title_mood_01 to R.string.notification_msg_mood_01,
                R.string.notification_title_mood_02 to R.string.notification_msg_mood_02,
                R.string.notification_title_mood_03 to R.string.notification_msg_mood_03,
                R.string.notification_title_mood_04 to R.string.notification_msg_mood_04,
                R.string.notification_title_mood_05 to R.string.notification_msg_mood_05,
                R.string.notification_title_mood_06 to R.string.notification_msg_mood_06,
                R.string.notification_title_mood_07 to R.string.notification_msg_mood_07,
                R.string.notification_title_mood_08 to R.string.notification_msg_mood_08,
                R.string.notification_title_mood_09 to R.string.notification_msg_mood_09,
                R.string.notification_title_mood_10 to R.string.notification_msg_mood_10,
            )
        val (titleId, messageId) = dataList.random()
        return NotificationData(
            EnumClass.NotificationType.Mood.channelId,
            R.drawable.ic_notification_mood,
            context.getString(titleId),
            context.getString(messageId),
            1002
        )
    }

    fun getAINotificationData(jsonString: String): NotificationData? {
        return try {
            val jsonObject = JSONObject(jsonString)

            NotificationData(
                EnumClass.NotificationType.AI.channelId,
                R.drawable.ic_notification_ai,
                jsonObject.getString("title"),
                jsonObject.getString("message"),
                1003
            )
        } catch (e: Exception) {
            LogModel.e(this, "getAINotificationData jsonString: $jsonString", e)
            null
        }
    }
}