package com.gyoheul.calm_routine.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MoodRecord(
    val date: String,
    val emotion: MoodEmotion,
    val memo: String,
    val prompt: String,
    val aiComment: String
) {
    fun toStoreMoodRecord(): StoreMoodRecord = StoreMoodRecord(
        date = date,
        emotion = Json.encodeToString(emotion),
        memo = memo,
        prompt = prompt,
        aiComment = aiComment
    )
}