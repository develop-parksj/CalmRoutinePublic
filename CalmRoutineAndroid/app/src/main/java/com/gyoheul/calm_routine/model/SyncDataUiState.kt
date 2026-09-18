package com.gyoheul.calm_routine.model

import com.gyoheul.calm_routine.common.EnumClass

data class SyncDataUiState(
    val autoBackupEnabled: Boolean = false,
    val backupFrequency: EnumClass.BackupFrequency = EnumClass.BackupFrequency.Daily,
    val lastSyncTime: String? = null,
    val isFrequencyMenuExpanded: Boolean = false,
    val backupFrequencyOptions: List<EnumClass.BackupFrequency> = EnumClass.BackupFrequency.entries.toList()
)