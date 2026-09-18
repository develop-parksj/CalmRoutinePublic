package com.gyoheul.calm_routine.model

data class HomeUiState(
    val todayRoutines: List<RoutineRecord> = emptyList(),
    val todayMood: MoodRecord? = null,
    val recentMoodRecords: List<MoodRecord> = emptyList(),
    val isLoading: Boolean = false
)
