package com.gyoheul.calm_routine.view.moodHistory

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.nativead.NativeAd
import com.gyoheul.calm_routine.common.Extension.getLogicalToday
import com.gyoheul.calm_routine.data.MobileAdsModel
import com.gyoheul.calm_routine.model.MoodHistoryUiState
import com.gyoheul.calm_routine.model.MoodRecord
import com.gyoheul.calm_routine.view.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MoodHistoryScreenViewModel(
    private val application: Application
) : BaseViewModel(application) {
    private val _uiState = MutableStateFlow(MoodHistoryUiState())
    val uiState: StateFlow<MoodHistoryUiState> = _uiState.asStateFlow()

    private val _nativeBottomAd = MutableStateFlow<NativeAd?>(null)
    val nativeBottomAd: StateFlow<NativeAd?> = _nativeBottomAd

    override suspend fun initialize() {
        isLoading.value = true

        super.initialize()
        loadMoodRecords()
        viewModelScope.launch {
            isLoading.value = false

            _nativeBottomAd.value = MobileAdsModel.loadNativeBottomAd(application)
        }
    }

    private fun loadMoodRecords() {
        _uiState.update { state ->
            val records = sqLiteManager.getMoods()

            state.copy(
                records = records,
                selectedMood = records.firstOrNull {
                    it.date == LocalDateTime.now().getLogicalToday().toString()
                },
                isLoading = false
            )
        }
    }

    fun onShowTypeChangeClick() {
        _uiState.update { state->
            state.copy(
                isShowCalendar = !state.isShowCalendar
            )
        }
    }

    fun onMoodClick(mood: MoodRecord?) {
        _uiState.update { state->
            state.copy(
                selectedMood = mood
            )
        }
    }
}