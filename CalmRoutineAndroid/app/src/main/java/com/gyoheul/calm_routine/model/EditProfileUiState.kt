package com.gyoheul.calm_routine.model

import com.gyoheul.calm_routine.common.EnumClass
import java.time.LocalDate

data class EditProfileUiState(
    val email: String = "",   // メールは読み取り専用
    val nickname: String = "",
    val gender: EnumClass.Gender = EnumClass.Gender.Unspecified,
    val birthDate: LocalDate? = null,
    val interests: List<String> = emptyList(),
    val interestInput: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)