package com.gyoheul.calm_routine.view.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.lifecycle.viewModelScope
import com.gyoheul.calm_routine.BuildConfig
import com.gyoheul.calm_routine.MainActivity
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.data.BackupManager
import com.gyoheul.calm_routine.data.NotificationModel
import com.gyoheul.calm_routine.model.AICommentSettingsUiState
import com.gyoheul.calm_routine.model.AccountSettingsUiState
import com.gyoheul.calm_routine.model.AppearanceSettingsUiState
import com.gyoheul.calm_routine.model.NotificationSettingsUiState
import com.gyoheul.calm_routine.model.OtherSettingsUiState
import com.gyoheul.calm_routine.model.SettingsUiState
import com.gyoheul.calm_routine.view.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.time.Duration.Companion.milliseconds

class SettingsScreenViewModel(
    private val application: Application
) : BaseViewModel(application) {
    private val backupManager = BackupManager(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    private val _accountUiState = MutableStateFlow(AccountSettingsUiState())
    val accountUiState: StateFlow<AccountSettingsUiState> = _accountUiState

    private val _notificationUiState = MutableStateFlow(NotificationSettingsUiState())
    val notificationUiState: StateFlow<NotificationSettingsUiState> = _notificationUiState

    private val _aiCommentUiState = MutableStateFlow(AICommentSettingsUiState())
    val aiCommentUiState: StateFlow<AICommentSettingsUiState> = _aiCommentUiState

    private val _appearanceUiState = MutableStateFlow(AppearanceSettingsUiState())
    val appearanceUiState: StateFlow<AppearanceSettingsUiState> = _appearanceUiState

    private val _otherUiState = MutableStateFlow(OtherSettingsUiState())
    val otherUiState: StateFlow<OtherSettingsUiState> = _otherUiState

    override suspend fun initialize() {
        isLoading.value = true
        super.initialize()

        viewModelScope.launch {
            loadAccountInfo()
            loadNotificationInfo()
            loadAICommentInfo()
            loadAppearanceInfo()
            loadOtherInfo()
            isLoading.value = false
        }
    }

    private fun loadAccountInfo() {
        _accountUiState.update {
            it.copy(
                userName = preferencesManager.getSettingsProfileData()?.nickname ?: user.value?.displayName ?: "",
                email = user.value?.email ?: "",
            )
        }
    }

    private fun loadNotificationInfo() {
        _notificationUiState.update {
            it.copy(
                notificationsEnabled = preferencesManager.getNotificationsEnabled(it.notificationsEnabled),
                notificationTime = preferencesManager.getNotificationTime(it.notificationTime),
                selectedTypes = preferencesManager.getNotificationTypes()
            )
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        preferencesManager.setNotificationsEnabled(enabled)
        _notificationUiState.update {
            it.copy(notificationsEnabled = enabled)
        }
        if (enabled) {
            setNotificationTime(
                _notificationUiState.value.notificationTime
            )
        } else {
            cancelNotification()
        }
    }

    fun setNotificationTime(time: LocalTime) {
        preferencesManager.setNotificationTime(time)
        _notificationUiState.update {
            it.copy(notificationTime = time)
        }
        setNotificationAlarm()
    }

    fun setNotificationTypes(typeSet: Set<EnumClass.NotificationType>) {
        preferencesManager.setNotificationTypes(typeSet)
        _notificationUiState.update {
            it.copy(selectedTypes = typeSet)
        }
        setNotificationAlarm()
    }

    private fun setNotificationAlarm() {
        val powerManager = application.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringOptimization = powerManager.isIgnoringBatteryOptimizations(application.packageName)

        if (isIgnoringOptimization) {
            NotificationModel.setNotificationAlarm(application)
        } else {
            _notificationUiState.update {
                it.copy(
                    showBatteryOptimizationDialog = true
                )
            }
        }
    }

    fun onBatteryOptimizationDialogDismiss() {
        _notificationUiState.update {
            it.copy(
                showBatteryOptimizationDialog = false
            )
        }
    }

    private fun cancelNotification() {
        NotificationModel.cancelDailyAlarm(application)
        NotificationModel.cancelAICoachingAlarms(application)
    }

    private fun loadAICommentInfo() {
        _aiCommentUiState.update {
            it.copy(
                style = preferencesManager.getAICommentStyle(it.style),
                frequency = preferencesManager.getAICommentFrequency(it.frequency),
            )
        }
    }

    fun setAICommentStyle(style: EnumClass.AICommentStyle) {
        preferencesManager.setAICommentStyle(style)
        _aiCommentUiState.update {
            it.copy(style = style)
        }
    }

    fun setAICommentFrequency(frequency: EnumClass.AICommentFrequency) {
        preferencesManager.setAICommentFrequency(frequency)
        _aiCommentUiState.update {
            it.copy(frequency = frequency)
        }
        NotificationModel.scheduleAICoachingAlarms(
            application,
            _notificationUiState.value.notificationTime,
            frequency
        )
    }

    fun loadAppearanceInfo() {
        _appearanceUiState.update {
            it.copy(
                themeMode = preferencesManager.getAppearanceThemeMode(it.themeMode),
                fontSize = preferencesManager.getAppearanceFontSize(it.fontSize),
                colorTheme = preferencesManager.getAppearanceColorTheme(it.colorTheme),
            )
        }
    }

    fun setAppearanceThemeMode(themeMode: EnumClass.ThemeMode) {
        preferencesManager.setAppearanceThemeMode(themeMode)
        _appearanceUiState.update {
            it.copy(themeMode = themeMode)
        }
    }

    fun setAppearanceFontSize(fontSize: EnumClass.FontSize) {
        preferencesManager.setAppearanceFontSize(fontSize)
        _appearanceUiState.update {
            it.copy(fontSize = fontSize)
        }
    }

    fun setAppearanceColorTheme(colorTheme: EnumClass.ColorTheme) {
        preferencesManager.setAppearanceColorTheme(colorTheme)
        _appearanceUiState.update {
            it.copy(colorTheme = colorTheme)
        }
    }

    suspend fun restoreUserData(): Boolean {
        return backupManager.restoreUserData().also {
            if (it) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1500.milliseconds)
                    restartApp()
                }
            }
        }
    }

    suspend fun backupUserData(): Boolean {
        return backupManager.backupUserData()
    }

    fun resetData(): Boolean {
        preferencesManager.clearAllData()
        sqLiteManager.clearAllData()
        CoroutineScope(Dispatchers.Main).launch {
            delay(1500.milliseconds)
            restartApp()
        }
        return true
    }

    private fun restartApp() {
        val intent = Intent(application, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        application.startActivity(intent)
    }

    private fun loadOtherInfo() {
        val versionName = BuildConfig.VERSION_NAME
        val versionCode = BuildConfig.VERSION_CODE
        val displayCode = versionCode.toFloat() % 100

        _otherUiState.update {
            it.copy(
                version = "v$versionName" + if (displayCode > 0) "+$displayCode" else ""
            )
        }
    }
}