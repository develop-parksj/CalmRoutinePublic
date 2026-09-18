package com.gyoheul.calm_routine.model

import com.gyoheul.calm_routine.common.EnumClass
import java.time.LocalTime

data class NotificationSettingsUiState(
    val notificationsEnabled: Boolean = false,
    val notificationTime: LocalTime = LocalTime.of(8, 0),
    val selectedTypes: Set<EnumClass.NotificationType> = setOf(EnumClass.NotificationType.Routine),
    val showBatteryOptimizationDialog: Boolean = false,
)
