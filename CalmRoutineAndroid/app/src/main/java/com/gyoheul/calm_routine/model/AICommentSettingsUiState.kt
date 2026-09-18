package com.gyoheul.calm_routine.model

import com.gyoheul.calm_routine.common.EnumClass

data class AICommentSettingsUiState(
    val style: EnumClass.AICommentStyle = EnumClass.AICommentStyle.Friendly,
    val frequency: EnumClass.AICommentFrequency = EnumClass.AICommentFrequency.Low
)
