package com.gyoheul.calm_routine.model

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.gyoheul.calm_routine.common.EnumClass

data class AppearanceSettingsUiState(
    val themeMode: EnumClass.ThemeMode = EnumClass.ThemeMode.System,
    val fontSize: EnumClass.FontSize = EnumClass.FontSize.Medium,
    val colorTheme: EnumClass.ColorTheme = EnumClass.ColorTheme.Blue
) {
    val isDark: Boolean
        @Composable
        get() = when(themeMode) {
            EnumClass.ThemeMode.Light -> false
            EnumClass.ThemeMode.Dark -> true
            EnumClass.ThemeMode.System -> isSystemInDarkTheme()
        }
}