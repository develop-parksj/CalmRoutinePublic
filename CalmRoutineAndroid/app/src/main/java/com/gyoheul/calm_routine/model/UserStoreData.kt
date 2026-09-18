package com.gyoheul.calm_routine.model

import com.google.firebase.Timestamp
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.common.Extension.getLogicalToday
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime

data class BackupDocument(
    val data: UserStoreData = UserStoreData(),
    val expireAt: Timestamp = Timestamp.now()
)

data class UserStoreData(
    val routines: List<StoreRoutineRecord> = listOf(),
    val moods: List<StoreMoodRecord> = listOf(),
    val settings: AppSettings = AppSettings(),
    val createAt: Timestamp = Timestamp.now()
)

data class StoreRoutineRecord(
    val date: String = LocalDateTime.now().getLogicalToday().toString(),
    val records: List<RoutineRecord> = listOf()
)

data class StoreMoodRecord(
    val date: String = LocalDateTime.now().getLogicalToday().toString(),
    val emotion: String = Json.encodeToString(EnumClass.EmotionType.Happy),
    val memo: String = "",
    val prompt: String = "",
    val aiComment: String = "",
)  {
    fun toMoodRecord(): MoodRecord = MoodRecord(
        date = date,
        emotion = Json.decodeFromString(emotion),
        memo = memo,
        prompt = prompt,
        aiComment = aiComment
    )
}

data class AppSettings(
    val profile: ProfileSettings = ProfileSettings(),
    val notification: NotificationSettings = NotificationSettings(),
    val aiComment: AICommentSettings = AICommentSettings(),
    val appearance: AppearanceSettings = AppearanceSettings(),
    val data: DataSettings = DataSettings()
)

data class ProfileSettings(
    val nickname: String = "",
    val gender: String = Json.encodeToString(EnumClass.Gender.Unspecified),
    val birthDate: String = "",
    val interests: List<String> = emptyList(),
) {
    fun toProfileData(): ProfileData = ProfileData(
        nickname = nickname,
        gender = Json.decodeFromString(gender),
        birthDate = if (birthDate.isBlank()) null else LocalDate.parse(birthDate),
        interests = interests
    )
}

data class NotificationSettings(
    val notificationsEnabled: Boolean = true,
    val notificationTime: String = "08:00",
    val notificationTypes: List<String> = listOf(EnumClass.NotificationType.Routine.name)
)

data class AICommentSettings(
    val style: String = EnumClass.AICommentStyle.Friendly.name,
    val frequency: String = EnumClass.AICommentFrequency.Low.name
)

data class AppearanceSettings(
    val themeMode: String = EnumClass.ThemeMode.System.name,
    val fontSize: String = EnumClass.FontSize.Medium.name,
    val colorTheme: String = EnumClass.ColorTheme.Blue.name,
)

data class DataSettings(
    val autoBackupEnabled: Boolean = false,
    val backupFrequency: String = EnumClass.BackupFrequency.Daily.name,
    val lastSyncTime: String? = null,
)