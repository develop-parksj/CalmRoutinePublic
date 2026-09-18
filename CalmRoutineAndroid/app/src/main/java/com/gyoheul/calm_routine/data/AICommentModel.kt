package com.gyoheul.calm_routine.data

import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.model.MoodEmotion
import com.gyoheul.calm_routine.model.MoodRecord
import com.gyoheul.calm_routine.model.ProfileData
import com.gyoheul.calm_routine.model.RoutineRecord
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period

object AICommentModel {
    fun getMoodSystemPrompt(style: EnumClass.AICommentStyle, profileData: ProfileData?): OpenAIManager.Message {
        val userContext = buildUserContext(profileData)

        val stylePrompt = when (style) {
            EnumClass.AICommentStyle.Friendly ->
                "You are a kind and supportive mental wellness coach who gently encourages users and makes them feel cared for."

            EnumClass.AICommentStyle.Motivational ->
                "You are a passionate and energetic mental wellness coach who motivates users with uplifting and action-oriented messages."

            EnumClass.AICommentStyle.Reflective ->
                "You are a thoughtful and insightful mental wellness coach who helps users reflect deeply on their thoughts and feelings with calm, introspective guidance."
        }

        return OpenAIManager.Message(
            role = "system",
            content = "$stylePrompt $userContext"
        )
    }

    private fun buildUserContext(profile: ProfileData?): String {
        if (profile == null) return ""

        val parts = mutableListOf<String>()

        // ニックネーム (愛称)
        if (profile.nickname.isNotBlank()) {
            parts += "The user's name is ${profile.nickname}."
        }

        // 性別
        when (profile.gender) {
            EnumClass.Gender.Male -> parts += "They identify as male."
            EnumClass.Gender.Female -> parts += "They identify as female."
            else -> {} // Unspecified → 無視
        }

        // 年齢計算
        if (profile.birthDate != null) {
            val age = Period.between(profile.birthDate, LocalDate.now()).years
            if (age in 5..120) {
                parts += "They are approximately $age years old."
            }
        }

        // 興味・関心
        if (profile.interests.isNotEmpty()) {
            val interestsStr = profile.interests.joinToString(", ")
            parts += "Their interests include $interestsStr."
        }

        return parts.joinToString(" ")
    }

    fun getHistoryMessagesByMoods(moods: List<MoodRecord>, includeToday: Boolean = false): List<OpenAIManager.Message> {
        return moods
            .filter {
                if (includeToday) {
                    true
                } else {
                    it.date != LocalDate.now().toString()
                }
            }
            .map { mood ->
            listOf(
                OpenAIManager.Message(
                    role = "user",
                    content = mood.prompt
                ),
                OpenAIManager.Message(
                    role = "assistant",
                    content = mood.aiComment
                )
            )
        }.flatten()
    }

    fun getUserMoodMessage(
        date: LocalDate = LocalDate.now(),
        mood: MoodEmotion,
        memo: String,
        style: EnumClass.AICommentStyle
    ): OpenAIManager.Message {
        val languageName = LocaleModel.getLanguageNameWithLanguageCode()
        return OpenAIManager.Message(
            role = "user",
            content = """
                ${
                when (style) {
                    EnumClass.AICommentStyle.Friendly ->
                        "The user feels ${mood.emoji}. They wrote: [$date] \"$memo\". " +
                                "Please respond with a short, gentle, and caring message (between 80 and 120 characters) that comforts and encourages them."

                    EnumClass.AICommentStyle.Motivational ->
                        "The user feels ${mood.emoji}. They wrote: [$date] \"$memo\". " +
                                "Please respond with a short and powerful message (between 80 and 120 characters) that boosts their motivation and energy."

                    EnumClass.AICommentStyle.Reflective ->
                        "The user feels ${mood.emoji}. They wrote: [$date] \"$memo\". " +
                                "Please respond with a calm and thoughtful message (between 80 and 120 characters) that helps them reflect on their thoughts and feelings."
                }
                }
                It is currently ${LocalTime.now()}.
                - If the input contains any language other than English, reply in that language. Otherwise, reply in ${languageName.ifBlank { "English" }}.
            """.trimIndent()
        )
    }

    fun getAICoachingSystemPrompt(style: EnumClass.AICommentStyle, routineMap: Map<LocalDate, List<RoutineRecord>>, profileData: ProfileData?): OpenAIManager.Message {
        val userContext = buildUserContext(profileData)
        val routineSummary = buildString {
            if (routineMap.isNotEmpty()) {
                append("User's recent routine completion status:\n\n")
                for ((date, routines) in routineMap) {
                    append("$date:\n")
                    for (r in routines) {
                        val status = if (r.isDone) "[Done]" else "[Not Done]"
                        append(" - $status ${r.title}\n")
                    }
                    append("\n")
                }
            }
        }
        val languageName = LocaleModel.getLanguageNameWithLanguageCode()
        val prompt = """
            ${
            when (style) {
                EnumClass.AICommentStyle.Friendly ->
                    "You are a kind and supportive mental wellness coach who gently encourages users and makes them feel cared for."

                EnumClass.AICommentStyle.Motivational ->
                    "You are a passionate and energetic mental wellness coach who motivates users with uplifting and action-oriented messages."

                EnumClass.AICommentStyle.Reflective ->
                    "You are a thoughtful and insightful mental wellness coach who helps users reflect deeply on their thoughts and feelings with calm, introspective guidance."
            }
            }
            
            $userContext
            
            $routineSummary
            
            - Return a JSON object with two fields: "title" (3–7 words) and "message" (35–60 characters). Please reply in ${languageName.ifBlank { "English" }}, and do not include anything outside the JSON object.
        """.trimIndent()
        return OpenAIManager.Message(
            role = "system",
            content = prompt
        )
    }

    fun getAICoachingMessage(
        style: EnumClass.AICommentStyle
    ): OpenAIManager.Message {
        return OpenAIManager.Message(
            role = "user",
            content = """
                ${
                when (style) {
                    EnumClass.AICommentStyle.Friendly ->
                        "Please give the user a short and warm message to uplift their day. Make it friendly, empathetic, and emotionally supportive."

                    EnumClass.AICommentStyle.Motivational ->
                        "Please give the user a short and energetic message to boost their confidence and help them stay motivated today."

                    EnumClass.AICommentStyle.Reflective ->
                        "Please give the user a short, calm message that encourages mindful reflection and emotional clarity today."
                }
                }
                It is currently ${LocalTime.now()}.
            """.trimIndent()
        )
    }
}