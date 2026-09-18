package com.gyoheul.calm_routine.data

import android.content.Context
import com.google.firebase.Timestamp
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.common.Extension.toUtcLocalDateTime
import com.gyoheul.calm_routine.model.AICommentSettings
import com.gyoheul.calm_routine.model.AppSettings
import com.gyoheul.calm_routine.model.AppearanceSettings
import com.gyoheul.calm_routine.model.DataSettings
import com.gyoheul.calm_routine.model.NotificationSettings
import com.gyoheul.calm_routine.model.ProfileData
import com.gyoheul.calm_routine.model.StoreRoutineRecord
import com.gyoheul.calm_routine.model.UserStoreData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class BackupManager(context: Context) {
    private val preferencesManager = SharedPreferencesManager(context)
    private val sqLiteManager = SQLiteManager(context)

    suspend fun backupUserData(): Boolean {
        return UserStoreData(
            routines = sqLiteManager.getRoutinesWithDateGroup().map { entry ->
                StoreRoutineRecord(
                    date = entry.key.toString(),
                    records = entry.value
                )
            },
            moods = sqLiteManager.getMoods().map {
                it.toStoreMoodRecord()
            },
            settings = AppSettings(
                profile = (preferencesManager.getSettingsProfileData() ?: ProfileData()).toProfileSettings(),
                notification = NotificationSettings(
                    notificationsEnabled = preferencesManager.getNotificationsEnabled(false),
                    notificationTime = preferencesManager
                        .getNotificationTime(LocalTime.of(8, 0))
                        .format(DateFormatModel.notificationTimeFormat),
                    notificationTypes = preferencesManager
                        .getNotificationTypes()
                        .map {
                            it.name
                        },
                ),
                aiComment = AICommentSettings(
                    style = preferencesManager.getAICommentStyle(
                        EnumClass.AICommentStyle.Friendly
                    ).name,
                    frequency = preferencesManager.getAICommentFrequency(
                        EnumClass.AICommentFrequency.Low
                    ).name
                ),
                appearance = AppearanceSettings(
                    themeMode = preferencesManager.getAppearanceThemeMode(
                        EnumClass.ThemeMode.System
                    ).name,
                    fontSize = preferencesManager.getAppearanceFontSize(
                        EnumClass.FontSize.Medium
                    ).name,
                    colorTheme = preferencesManager.getAppearanceColorTheme(
                        EnumClass.ColorTheme.Blue
                    ).name
                )
            )
        ).let {
            LogModel.d(this, "backupUserData : $it")

            FirebaseModel.backupUserData(it)
        }
    }

    suspend fun restoreUserData(): Boolean {
        val userStoreData: UserStoreData? = FirebaseModel.restoreUserData()
        return userStoreData?.also { data ->
            LogModel.d(this, "restoreUserData : $data")

            preferencesManager.clearAllData()
            sqLiteManager.clearAllData()

            data.routines.forEach { routine ->
                sqLiteManager.upsertRoutines(routine.records, LocalDate.parse(routine.date))
            }
            sqLiteManager.upsertMoods(data.moods.map { it.toMoodRecord() })

            preferencesManager.setSettingsProfileData(data.settings.profile.toProfileData())

            val notification: NotificationSettings = data.settings.notification
            preferencesManager.setNotificationsEnabled(notification.notificationsEnabled)
            preferencesManager.setNotificationTime(
                LocalTime.parse(notification.notificationTime, DateFormatModel.notificationTimeFormat)
            )
            preferencesManager.setNotificationTypes(
                notification.notificationTypes.map { notificationTypeName ->
                    EnumClass.NotificationType.entries.first { it.name == notificationTypeName }
                }.toSet()
            )

            val aiComment: AICommentSettings = data.settings.aiComment
            preferencesManager.setAICommentStyle(
                EnumClass.AICommentStyle.entries.first { it.name == aiComment.style }
            )
            preferencesManager.setAICommentFrequency(
                EnumClass.AICommentFrequency.entries.first { it.name == aiComment.frequency }
            )

            val appearance: AppearanceSettings = data.settings.appearance
            preferencesManager.setAppearanceThemeMode(
                EnumClass.ThemeMode.entries.first { it.name == appearance.themeMode }
            )
            preferencesManager.setAppearanceFontSize(
                EnumClass.FontSize.entries.first { it.name == appearance.fontSize }
            )
            preferencesManager.setAppearanceColorTheme(
                EnumClass.ColorTheme.entries.first { it.name == appearance.colorTheme }
            )

            val dataSettings: DataSettings = data.settings.data
            preferencesManager.setSyncDataAutoBackupEnabled(
                dataSettings.autoBackupEnabled
            )
            preferencesManager.setSyncDataBackupFrequency(
                EnumClass.BackupFrequency.entries.first { it.name == dataSettings.backupFrequency }
            )
            preferencesManager.setSyncDataLastSyncTime(
                dataSettings.lastSyncTime?.let {
                    LocalDateTime.parse(it)
                }
            )
        } != null
    }

    suspend fun syncUserData(): Boolean {
        val userStoreData: UserStoreData? = FirebaseModel.restoreUserData()
        return userStoreData?.also { data ->
            LogModel.d(this, "syncUserData : $data")

            val now = LocalDateTime.now()
            val createAt: Timestamp = data.createAt
            val lastModifiedDate: LocalDateTime = preferencesManager.getLastModifiedDate()
            if (createAt.toDate().toUtcLocalDateTime().isBefore(lastModifiedDate)) {
                preferencesManager.setSyncDataLastSyncTime(now)
                return true
            }

            data.routines.forEach { routine ->
                sqLiteManager.upsertRoutines(routine.records, LocalDate.parse(routine.date))
            }
            sqLiteManager.upsertMoods(data.moods.map { it.toMoodRecord() })

            preferencesManager.setSettingsProfileData(data.settings.profile.toProfileData())

            val notification: NotificationSettings = data.settings.notification
            preferencesManager.setNotificationsEnabled(notification.notificationsEnabled)
            preferencesManager.setNotificationTime(
                LocalTime.parse(notification.notificationTime, DateFormatModel.notificationTimeFormat)
            )
            preferencesManager.setNotificationTypes(
                notification.notificationTypes.map { notificationTypeName ->
                    EnumClass.NotificationType.entries.first { it.name == notificationTypeName }
                }.toSet()
            )

            val aiComment: AICommentSettings = data.settings.aiComment
            preferencesManager.setAICommentStyle(
                EnumClass.AICommentStyle.entries.first { it.name == aiComment.style }
            )
            preferencesManager.setAICommentFrequency(
                EnumClass.AICommentFrequency.entries.first { it.name == aiComment.frequency }
            )

            val appearance: AppearanceSettings = data.settings.appearance
            preferencesManager.setAppearanceThemeMode(
                EnumClass.ThemeMode.entries.first { it.name == appearance.themeMode }
            )
            preferencesManager.setAppearanceFontSize(
                EnumClass.FontSize.entries.first { it.name == appearance.fontSize }
            )
            preferencesManager.setAppearanceColorTheme(
                EnumClass.ColorTheme.entries.first { it.name == appearance.colorTheme }
            )

            val dataSettings: DataSettings = data.settings.data
            preferencesManager.setSyncDataAutoBackupEnabled(
                dataSettings.autoBackupEnabled
            )
            preferencesManager.setSyncDataBackupFrequency(
                EnumClass.BackupFrequency.entries.first { it.name == dataSettings.backupFrequency }
            )
            preferencesManager.setSyncDataLastSyncTime(now)
        } != null
    }
}