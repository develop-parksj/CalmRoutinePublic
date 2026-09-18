package com.gyoheul.calm_routine.data

import android.content.Context
import android.content.SharedPreferences
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.common.Extension.getLogicalToday
import com.gyoheul.calm_routine.model.MoodEmotion
import com.gyoheul.calm_routine.model.ProfileData
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import androidx.core.content.edit

class SharedPreferencesManager(context: Context) {
    companion object {
        private const val KEY_MOOD_RECENT_USE_LIST = "MoodRecentUseList"
        private const val KEY_MOOD_CUSTOM_MOOD_SET = "MoodCustomMoodSet"
        private const val KEY_MOOD_TODAY_SAVED_COUNT = "MoodTodaySavedCount"
        private const val KEY_LAST_MODIFIED_DATE = "LastModifiedDate"
        private const val KEY_LAST_DELETE_DATA_DATE = "LastDeleteDataDate"

        private const val KEY_SETTINGS_PROFILE_DATA = "ProfileData"
        private const val KEY_SETTINGS_NOTIFICATIONS_ENABLED = "NotificationsEnabled"
        private const val KEY_SETTINGS_NOTIFICATION_TIME = "NotificationTime"
        private const val KEY_SETTINGS_NOTIFICATION_TYPES = "NotificationTypes"
        private const val KEY_SETTINGS_AI_COMMENT_STYLE = "AICommentStyle"
        private const val KEY_SETTINGS_AI_COMMENT_FREQUENCY = "AICommentFrequency"
        private const val KEY_SETTINGS_APPEARANCE_THEME_MODE = "AppearanceThemeMode"
        private const val KEY_SETTINGS_APPEARANCE_FONT_SIZE = "AppearanceFontSize"
        private const val KEY_SETTINGS_APPEARANCE_COLOR_THEME = "AppearanceColorTheme"
        private const val KEY_SETTINGS_SYNC_DATA_AUTO_BACKUP_ENABLED = "SyncDataAutoBackupEnabled"
        private const val KEY_SETTINGS_SYNC_DATA_BACKUP_FREQUENCY = "SyncDataBackupFrequency"
        private const val KEY_SETTINGS_SYNC_DATA_LAST_SYNC_TIME = "SyncDataLastSyncTime"
    }

    private val preferences: SharedPreferences =
        context.getSharedPreferences("CalmRoutinePrefs", Context.MODE_PRIVATE)

    private val settingsPreferences: SharedPreferences =
        context.getSharedPreferences("CalmRoutineSettingsPrefs", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true // 今後のモデル変更に備える
        encodeDefaults = true    // デフォルト値を含めてシリアル化
        prettyPrint = false      // 保存用なのでfalse
    }

    private val moodTodaySavedCountKey
        get() = "$KEY_MOOD_TODAY_SAVED_COUNT${LocalDateTime.now().getLogicalToday()}"

    private inline fun editAndUpdate(
        prefs: SharedPreferences = preferences,
        block: SharedPreferences.Editor.() -> Unit
    ) {
        prefs.edit().apply {
            block()
            putString(
                KEY_LAST_MODIFIED_DATE,
                LocalDateTime.now().toString()
            )
            apply()
        }
    }

    fun getLastModifiedDate(): LocalDateTime {
        val dateString: String? = preferences.getString(KEY_LAST_MODIFIED_DATE, null)
        return dateString?.let {
            LocalDateTime.parse(it)
        } ?: LocalDateTime.MIN
    }

    fun setLastDeleteDataDate() {
        preferences.edit { putString(KEY_LAST_DELETE_DATA_DATE, LocalDate.now().toString()) }
    }

    fun getLastDeleteDataDate(): LocalDate {
        val dateString: String? = preferences.getString(KEY_LAST_DELETE_DATA_DATE, null)
        return dateString?.let {
            LocalDate.parse(it)
        } ?: LocalDate.MIN
    }

    fun addCustomMood(mood: MoodEmotion.Custom) {
        val moods = getCustomMoods().toMutableSet()
        moods.add(mood)
        editAndUpdate {
            putString(KEY_MOOD_CUSTOM_MOOD_SET, json.encodeToString(moods))
        }
    }

    fun getCustomMoods(): Set<MoodEmotion.Custom> {
        val jsonString = preferences.getString(KEY_MOOD_CUSTOM_MOOD_SET, null) ?: return emptySet()
        return try {
            json.decodeFromString(jsonString)
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun setRecentMood(mood: MoodEmotion) {
        val moods = getRecentMoods().toMutableList().apply {
            remove(mood)
            add(0, mood)
        }

        val json = json.encodeToString(moods)
        editAndUpdate {
            putString(KEY_MOOD_RECENT_USE_LIST, json)
        }
    }

    fun getRecentMoods(): List<MoodEmotion> {
        val jsonString = preferences.getString(KEY_MOOD_RECENT_USE_LIST, null) ?: return emptyList()
        return try {
            json.decodeFromString(jsonString)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addMoodToadySavedCount() {
        val todayCount: Int = getMoodToadySavedCount()
        editAndUpdate {
            putInt(moodTodaySavedCountKey, todayCount + 1)
        }
    }

    fun getMoodToadySavedCount(): Int {
        return preferences.getInt(moodTodaySavedCountKey, 0)
    }

    fun removeOldMoodTodayCountKeys() {
        val keysToRemove = preferences.all.keys.filter { key ->
            key.startsWith(KEY_MOOD_TODAY_SAVED_COUNT) && key != moodTodaySavedCountKey
        }
        if (keysToRemove.isNotEmpty()) {
            preferences.edit {
                keysToRemove.forEach { remove(it) }
            }
        }
    }

    fun setSettingsProfileData(profileData: ProfileData) {
        editAndUpdate {
            putString(KEY_SETTINGS_PROFILE_DATA, json.encodeToString(profileData))
        }
    }

    fun getSettingsProfileData(): ProfileData? {
        val jsonString = preferences.getString(KEY_SETTINGS_PROFILE_DATA, null) ?: return null
        return try {
            json.decodeFromString(jsonString)
        } catch (_: Exception) {
            null
        }
    }

    fun setNotificationsEnabled(notificationsEnabled: Boolean) {
        editAndUpdate(
            settingsPreferences
        ) {
            putBoolean(KEY_SETTINGS_NOTIFICATIONS_ENABLED, notificationsEnabled)
        }
    }

    fun getNotificationsEnabled(default: Boolean): Boolean {
        return settingsPreferences.getBoolean(KEY_SETTINGS_NOTIFICATIONS_ENABLED, default)
    }

    fun setNotificationTime(notificationTime: LocalTime) {
        editAndUpdate(
            settingsPreferences
        ) {
            putString(
                KEY_SETTINGS_NOTIFICATION_TIME,
                notificationTime.format(DateFormatModel.notificationTimeFormat)
            )
        }
    }

    fun getNotificationTime(default: LocalTime): LocalTime {
        val notificationTimeString: String? = settingsPreferences.getString(
            KEY_SETTINGS_NOTIFICATION_TIME,
            null
        )
        return notificationTimeString?.let {
            LocalTime.parse(notificationTimeString, DateFormatModel.notificationTimeFormat)
        } ?: default
    }

    fun setNotificationTypes(typeSet: Set<EnumClass.NotificationType>) {
        val jsonString = json.encodeToString(typeSet.toList())

        editAndUpdate(
            settingsPreferences
        ) {
            putString(KEY_SETTINGS_NOTIFICATION_TYPES, jsonString)
        }
    }

    fun getNotificationTypes(): Set<EnumClass.NotificationType> {
        val jsonString = settingsPreferences.getString(KEY_SETTINGS_NOTIFICATION_TYPES, null) ?: return emptySet()
        return try {
            json.decodeFromString(jsonString)
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun setAICommentStyle(style: EnumClass.AICommentStyle) {
        editAndUpdate(
            settingsPreferences
        ) {
            putString(KEY_SETTINGS_AI_COMMENT_STYLE, style.name)
        }
    }

    fun getAICommentStyle(default: EnumClass.AICommentStyle): EnumClass.AICommentStyle {
        val aiCommentStyleName: String = settingsPreferences.getString(
            KEY_SETTINGS_AI_COMMENT_STYLE,
            default.name
        ) ?: default.name
        return EnumClass.AICommentStyle.entries.first { it.name == aiCommentStyleName }
    }

    fun setAICommentFrequency(frequency: EnumClass.AICommentFrequency) {
        editAndUpdate(
            settingsPreferences
        ) {
            putString(KEY_SETTINGS_AI_COMMENT_FREQUENCY, frequency.name)
        }
    }

    fun getAICommentFrequency(default: EnumClass.AICommentFrequency): EnumClass.AICommentFrequency {
        val aiCommentFrequencyName: String = settingsPreferences.getString(
            KEY_SETTINGS_AI_COMMENT_FREQUENCY,
            default.name
        ) ?: default.name
        return EnumClass.AICommentFrequency.entries.first { it.name == aiCommentFrequencyName }
    }

    fun setAppearanceThemeMode(themeMode: EnumClass.ThemeMode) {
        editAndUpdate(
            settingsPreferences
        ) {
            putString(KEY_SETTINGS_APPEARANCE_THEME_MODE, themeMode.name)
        }
    }

    fun getAppearanceThemeMode(default: EnumClass.ThemeMode): EnumClass.ThemeMode {
        val themeModeName: String = settingsPreferences.getString(
            KEY_SETTINGS_APPEARANCE_THEME_MODE,
            default.name
        ) ?: default.name
        return EnumClass.ThemeMode.entries.first { it.name == themeModeName }
    }

    fun setAppearanceFontSize(fontSize: EnumClass.FontSize) {
        editAndUpdate(
            settingsPreferences
        ) {
            putString(KEY_SETTINGS_APPEARANCE_FONT_SIZE, fontSize.name)
        }
    }

    fun getAppearanceFontSize(default: EnumClass.FontSize): EnumClass.FontSize {
        val fontSizeName: String = settingsPreferences.getString(
            KEY_SETTINGS_APPEARANCE_FONT_SIZE,
            default.name
        ) ?: default.name
        return EnumClass.FontSize.entries.first { it.name == fontSizeName }
    }

    fun setAppearanceColorTheme(colorTheme: EnumClass.ColorTheme) {
        editAndUpdate(
            settingsPreferences
        ) {
            putString(KEY_SETTINGS_APPEARANCE_COLOR_THEME, colorTheme.name)
        }
    }

    fun getAppearanceColorTheme(default: EnumClass.ColorTheme): EnumClass.ColorTheme {
        val colorThemeName: String = settingsPreferences.getString(
            KEY_SETTINGS_APPEARANCE_COLOR_THEME,
            default.name
        ) ?: default.name
        return EnumClass.ColorTheme.entries.first { it.name == colorThemeName }
    }

    fun setSyncDataAutoBackupEnabled(autoBackupEnabled: Boolean) {
        editAndUpdate(
            settingsPreferences
        ) {
            putBoolean(KEY_SETTINGS_SYNC_DATA_AUTO_BACKUP_ENABLED, autoBackupEnabled)
        }
    }

    fun getSyncDataAutoBackupEnabled(): Boolean {
        return settingsPreferences.getBoolean(KEY_SETTINGS_SYNC_DATA_AUTO_BACKUP_ENABLED, false)
    }

    fun setSyncDataBackupFrequency(frequency: EnumClass.BackupFrequency) {
        editAndUpdate(
            settingsPreferences
        ) {
            putString(KEY_SETTINGS_SYNC_DATA_BACKUP_FREQUENCY, frequency.name)
        }
    }

    fun getSyncDataBackupFrequency(default: EnumClass.BackupFrequency): EnumClass.BackupFrequency {
        val frequencyName: String = settingsPreferences.getString(
            KEY_SETTINGS_SYNC_DATA_BACKUP_FREQUENCY,
            default.name
        ) ?: default.name
        return EnumClass.BackupFrequency.entries.first { it.name == frequencyName }
    }

    fun setSyncDataLastSyncTime(lastSyncTime: LocalDateTime?) {
        editAndUpdate(
            settingsPreferences
        ) {
            putString(
                KEY_SETTINGS_SYNC_DATA_LAST_SYNC_TIME,
                lastSyncTime?.toString()
            )
        }
    }

    fun getSyncDataLastSyncTime(default: LocalDateTime?): LocalDateTime? {
        val syncTimeString: String? = settingsPreferences.getString(KEY_SETTINGS_SYNC_DATA_LAST_SYNC_TIME, null)
        return syncTimeString?.let {
            LocalDateTime.parse(it)
        } ?: default
    }

    fun clearAllData() {
        preferences.edit { clear() }
        settingsPreferences.edit { clear() }
    }
}