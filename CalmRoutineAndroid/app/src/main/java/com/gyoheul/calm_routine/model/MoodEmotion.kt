package com.gyoheul.calm_routine.model

import com.gyoheul.calm_routine.common.EnumClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class MoodEmotion {
    abstract val emoji: String

    @Serializable
    @SerialName("predefined")
    data class Predefined(val emotion: EnumClass.EmotionType) : MoodEmotion() {
        override val emoji get() = emotion.emoji
    }
    @Serializable
    @SerialName("custom")
    data class Custom(override val emoji: String) : MoodEmotion()
}