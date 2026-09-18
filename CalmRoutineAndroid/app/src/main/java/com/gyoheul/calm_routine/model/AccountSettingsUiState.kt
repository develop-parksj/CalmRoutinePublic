package com.gyoheul.calm_routine.model

data class AccountSettingsUiState(
    val userName: String = "User123",
    val email: String = "user@example.com",
    var isShowLogoutDialog: Boolean = false
)
