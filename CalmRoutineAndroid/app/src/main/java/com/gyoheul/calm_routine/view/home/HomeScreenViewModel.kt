package com.gyoheul.calm_routine.view.home

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.gyoheul.calm_routine.common.Extension.getLogicalToday
import com.gyoheul.calm_routine.model.HomeUiState
import com.gyoheul.calm_routine.view.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class HomeScreenViewModel(
    application: Application
) : BaseViewModel(application) {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    override suspend fun initialize() {
        isLoading.value = true

        super.initialize()
        loadHomeInfo()
        viewModelScope.launch {
            isLoading.value = false
        }
    }

    private fun loadHomeInfo() {
        viewModelScope.launch {
            val routines = sqLiteManager.getTodayRoutines()
            val records = sqLiteManager.getMoods(5)
            val todayMood = records.firstOrNull {
                it.date == LocalDateTime.now().getLogicalToday().toString()
            }

            _uiState.update { state ->
                state.copy(
                    todayRoutines = routines,
                    todayMood = todayMood,
                    recentMoodRecords = records,
                    isLoading = false
                )
            }
        }
    }
}