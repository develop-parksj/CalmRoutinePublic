package com.gyoheul.calm_routine.model

data class MoodUiState(
    val selectedMood: MoodEmotion? = null,
    val memo: String = "",
    val isLoading: Boolean = false,
    val aiComment: String = "",
    val todaySavedCount: Int = 0
)