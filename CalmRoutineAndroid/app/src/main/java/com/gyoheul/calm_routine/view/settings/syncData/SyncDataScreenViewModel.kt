package com.gyoheul.calm_routine.view.settings.syncData

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.ads.nativead.NativeAd
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.data.BackupManager
import com.gyoheul.calm_routine.data.DateFormatModel
import com.gyoheul.calm_routine.data.MobileAdsModel
import com.gyoheul.calm_routine.data.SyncDataWorker
import com.gyoheul.calm_routine.data.ToastModel
import com.gyoheul.calm_routine.model.SyncDataUiState
import com.gyoheul.calm_routine.view.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class SyncDataScreenViewModel(
    private val application: Application
) : BaseViewModel(application) {
    private val backupManager = BackupManager(application)

    private val _uiState = MutableStateFlow(SyncDataUiState())
    val uiState: StateFlow<SyncDataUiState> = _uiState

    private val _nativeBottomAd = MutableStateFlow<NativeAd?>(null)
    val nativeBottomAd: StateFlow<NativeAd?> = _nativeBottomAd

    override suspend fun initialize() {
        isLoading.value = true

        super.initialize()
        loadSyncDataInfo()
        viewModelScope.launch {
            isLoading.value = false

            _nativeBottomAd.value = MobileAdsModel.loadNativeBottomAd(application)
        }
    }

    private fun loadSyncDataInfo() {
        _uiState.update {
            it.copy(
                autoBackupEnabled = preferencesManager.getSyncDataAutoBackupEnabled(),
                backupFrequency = preferencesManager.getSyncDataBackupFrequency(it.backupFrequency),
                lastSyncTime = preferencesManager.getSyncDataLastSyncTime(
                    it.lastSyncTime?.let { syncTime ->
                        LocalDateTime.parse(syncTime, DateFormatModel.syncDataDateFormat)
                    }
                )?.format(DateFormatModel.syncDataDateFormat)
            )
        }
    }

    fun onAutoBackupEnabledClick(enabled: Boolean) {
        preferencesManager.setSyncDataAutoBackupEnabled(enabled)
        if (enabled) {
            setSyncDataWorkManager(
                _uiState.value.backupFrequency
            )
        } else {
            cancelSyncDataWorkManager()
        }
        _uiState.update {
            it.copy(autoBackupEnabled = enabled)
        }
    }

    fun onBackupFrequencyClick(frequency: EnumClass.BackupFrequency) {
        preferencesManager.setSyncDataBackupFrequency(frequency)
        setSyncDataWorkManager(frequency)
        _uiState.update {
            it.copy(backupFrequency = frequency)
        }
    }

    private fun setSyncDataWorkManager(backupFrequency: EnumClass.BackupFrequency) {
        val periodicWorkRequest =
            PeriodicWorkRequestBuilder<SyncDataWorker>(
                backupFrequency.repeatInterval,
                TimeUnit.DAYS
            ).build()

        WorkManager.getInstance(application).enqueueUniquePeriodicWork(
            "periodic_data_sync",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
        viewModelScope.launch {
            backupManager.backupUserData()
        }
    }

    private fun cancelSyncDataWorkManager() {
        WorkManager.getInstance(application).cancelUniqueWork("periodic_data_sync")
    }

    fun setFrequencyMenuExpanded(expanded: Boolean) {
        _uiState.update {
            it.copy(isFrequencyMenuExpanded = expanded)
        }
    }

    fun onManualSyncClick() {
        isLoading.value = true
        viewModelScope.launch {
            val result = backupManager.syncUserData()
            CoroutineScope(Dispatchers.Main).launch {
                isLoading.value = false
                ToastModel.showToast(
                    application,
                    if (result) {
                        val syncDateTime = preferencesManager.getSyncDataLastSyncTime(LocalDateTime.now())
                        _uiState.update {
                            it.copy(
                                lastSyncTime = syncDateTime?.format(DateFormatModel.syncDataDateFormat)
                            )
                        }

                        EnumClass.ToastType.SuccessSyncData
                    } else {
                        EnumClass.ToastType.FailSyncData
                    }
                )
            }
        }
    }
}
