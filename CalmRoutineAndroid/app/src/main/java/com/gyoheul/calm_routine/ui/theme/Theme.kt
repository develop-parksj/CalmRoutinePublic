package com.gyoheul.calm_routine.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.gyoheul.calm_routine.common.EnumClass

val LocalMainColorScheme = staticCompositionLocalOf { lightColorScheme() }

private val LightColorScheme = lightColorScheme(
    primary = A100LightBlue,
    secondary = A100LightGreen,
    background = IvoryBackground,
    surface = LightSurface,
    onPrimary = Color(0xFF0D47A1),            // 濃い青 (テキストなど)
    onSecondary = Color(0xFF004D40),          // 濃い緑
    onBackground = Color(0xFF333333),         // 濃い灰色のテキスト
    onSurface = Color(0xFF333333),
    error = Color(0xFFB00020),
    secondaryContainer = A100LightCyan,
    onSecondaryContainer = Color(0xFF004D40),
)

private val DarkColorScheme = darkColorScheme(
    primary = A700DarkBlue,
    secondary = A700DarkGreen,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color(0xFFE3F2FD),            // 薄い青のテキスト
    onSecondary = Color(0xFFA5D6A7),          // 薄い緑のテキスト
    onBackground = Color(0xFFEEEEEE),
    onSurface = Color(0xFFEEEEEE),
    error = Color(0xFFCF6679),
    secondaryContainer = Color(0xFF004D40),
    onSecondaryContainer = Color(0xFFE0F2F1),
)

@Composable
fun CalmRoutineTheme(
    themeMode: EnumClass.ThemeMode,
    fontSize: EnumClass.FontSize,
    colorTheme: EnumClass.ColorTheme,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        EnumClass.ThemeMode.Light -> false
        EnumClass.ThemeMode.Dark -> true
        EnumClass.ThemeMode.System -> systemDark
    }
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
    val textColor = if (isDarkTheme) TextColorOnDark else TextColorOnLight

    fun TextStyle.scaledWithColor(
        scaleFactor: Float,
        color: Color
    ) = copy(
        fontSize = this.fontSize * scaleFactor,
        color = color
    )

    val typography = Typography(
        bodyLarge = typography.bodyLarge.scaledWithColor(fontSize.scaleFactor, textColor),
        bodyMedium = typography.bodyMedium.scaledWithColor(fontSize.scaleFactor, textColor),
        bodySmall = typography.bodySmall.scaledWithColor(fontSize.scaleFactor, textColor),
        titleLarge = typography.titleLarge.scaledWithColor(fontSize.scaleFactor, textColor),
        titleMedium = typography.titleMedium.scaledWithColor(fontSize.scaleFactor, textColor),
        titleSmall = typography.titleSmall.scaledWithColor(fontSize.scaleFactor, textColor),
        labelLarge = typography.labelLarge.scaledWithColor(fontSize.scaleFactor, textColor),
        labelMedium = typography.labelMedium.scaledWithColor(fontSize.scaleFactor, textColor),
        labelSmall = typography.labelSmall.scaledWithColor(fontSize.scaleFactor, textColor),
    )
    val premiumTextColor = if (isDarkTheme) Color.White else Color(0xFF6A1B9A)
    val premiumBackground = if (isDarkTheme) Color(0xFF311B92) else Color(0xFFDCCEF9)

    CompositionLocalProvider(
        LocalTextColor provides textColor,
        LocalMainColorScheme provides colorScheme,
        LocalMainTypography provides typography,
        LocalPremiumColor provides PremiumColors(
            text = premiumTextColor,
            background = premiumBackground
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}