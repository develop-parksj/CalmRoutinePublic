package com.gyoheul.calm_routine.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ライトモードの主要カラー
val A100LightBlue = Color(0xFFBBDEFB)   // Light Blue A100
val A100LightGreen = Color(0xFFB9F6CA)  // Light Green A100
val A100LightCyan = Color(0xFF80DEEA)   // Cyan A100

// ダークモードの主要カラー (トーンダウン)
val A700DarkBlue = Color(0xFF1976D2)    // Blue A700
val A700DarkGreen = Color(0xFF00C853)   // Green A700
val A700DarkCyan = Color(0xFF00ACC1)    // Cyan A700

// 背景および表面の色
val PastelSkyBlue = Color(0xFFCCE5F6)
val PastelGreen = Color(0xFFB9F6CA)
val PastelPink = Color(0xFFF8BBD0)
val IvoryBackground = Color(0xFFFFF8E7)     // アイボリー、目に優しい
val DarkBackground = Color(0xFF121212)       // ダーク背景、完全な黒ではない
val LightSurface = Color(0xFFFFFFFF)          // 白色の表面
val DarkSurface = Color(0xFF1E1E1E)           // ダーク表面

val LoadingColor = Color(0xB3000000)
val Red = Color(0xFFFF5252) // Red A200
val Blue = Color(0xFF536DFE) // Blue A200

val TextColorOnDark = Color(0xFFEEEEEE)
val TextColorOnLight = Color(0xFF333333)

val LocalTextColor = staticCompositionLocalOf { Color.Unspecified }

data class PremiumColors(val text: Color, val background: Color)

val LocalPremiumColor = staticCompositionLocalOf { PremiumColors(Color.Unspecified, Color.Unspecified) }