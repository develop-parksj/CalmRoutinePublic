package com.gyoheul.calm_routine.model

import com.gyoheul.calm_routine.common.EnumClass

data class EmotionUiState(
    val emotionList: List<MoodEmotion> = EnumClass.EmotionType.entries.map {
        MoodEmotion.Predefined(it)
    }
)
