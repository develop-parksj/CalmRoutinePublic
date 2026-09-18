package com.gyoheul.calm_routine.model

data class MoodHistoryUiState(
    val records: List<MoodRecord> = emptyList(),
    val isShowCalendar: Boolean = false,
    val selectedMood: MoodRecord? = null,
    val isLoading: Boolean = false
)