package com.gyoheul.calm_routine.model

import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.data.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

@Serializable
data class ProfileData(
    val nickname: String = "",
    val gender: EnumClass.Gender = EnumClass.Gender.Unspecified,
    @Serializable(with = LocalDateSerializer::class)
    val birthDate: LocalDate? = null,
    val interests: List<String> = emptyList(),
) {
    fun toProfileSettings(): ProfileSettings = ProfileSettings(
        nickname = nickname,
        gender = Json.encodeToString(gender),
        birthDate = birthDate?.toString() ?: "",
        interests = interests
    )
}
