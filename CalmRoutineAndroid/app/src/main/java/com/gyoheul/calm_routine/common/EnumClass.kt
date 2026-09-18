package com.gyoheul.calm_routine.common

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.gyoheul.calm_routine.R
import com.gyoheul.calm_routine.ui.theme.A700DarkBlue
import com.gyoheul.calm_routine.ui.theme.A700DarkGreen
import com.gyoheul.calm_routine.ui.theme.Blue
import com.gyoheul.calm_routine.ui.theme.PastelGreen
import com.gyoheul.calm_routine.ui.theme.PastelPink
import com.gyoheul.calm_routine.ui.theme.PastelSkyBlue
import com.gyoheul.calm_routine.ui.theme.Red
import kotlinx.serialization.Serializable

object EnumClass {
    @Serializable
    enum class EmotionType(val emoji: String) {
        Happy("😊"),
        Sad("😢"),
        Angry("😠"),
        Tired("😩"),
        Calm("😌"),
        Neutral("😐"),
        Excited("🤩"),
        Anxious("😰"),
        Confused("😕"),
        Bored("😒"),
        Lonely("😔"),
        Grateful("🙏"),
        Surprised("😮"),
        Love("😍"),
        Sleepy("😴"),
        Frustrated("😤")
    }

    @Serializable
    enum class NotificationType(@StringRes val labelRes: Int) {
        Routine(R.string.settings_notification_type_routine),
        Mood(R.string.settings_notification_type_mood),
        AI(R.string.settings_notification_type_ai)
    }
    
    enum class AICommentStyle(@StringRes val labelRes: Int) {
        Friendly(R.string.settings_ai_comment_style_friendly),
        Motivational(R.string.settings_ai_comment_style_motivational),
        Reflective(R.string.settings_ai_comment_style_reflective)
    }

    enum class AICommentFrequency(@StringRes val labelRes: Int) {
        High(R.string.settings_ai_comment_frequency_high),
        Medium(R.string.settings_ai_comment_frequency_medium),
        Low(R.string.settings_ai_comment_frequency_low)
    }

    enum class ThemeMode(@StringRes val labelRes: Int) {
        Light(R.string.settings_theme_mode_light),
        Dark(R.string.settings_theme_mode_dark),
        System(R.string.settings_theme_mode_system)
    }

    enum class FontSize(@StringRes val labelRes: Int, val scaleFactor: Float) {
        Small(R.string.settings_font_size_small, 0.85f),
        Medium(R.string.settings_font_size_medium, 1.0f),
        Large(R.string.settings_font_size_large, 1.15f)
    }

    enum class ColorTheme(
        @StringRes val labelRes: Int,
        val lightPrimary: Color,
        val darkPrimary: Color
    ) {
        Blue(R.string.settings_color_theme_blue, PastelSkyBlue, A700DarkBlue),
        Mint(R.string.settings_color_theme_mint, PastelGreen, A700DarkGreen),
        Pink(R.string.settings_color_theme_pink, PastelPink, Color(0xFFAD1457))
    }

    enum class BackupFrequency(@StringRes val labelRes: Int, val repeatInterval: Long) {
        Daily(R.string.settings_sync_data_frequency_daily, 1L),
        Weekly(R.string.settings_sync_data_frequency_weekly, 7L),
        Monthly(R.string.settings_sync_data_frequency_monthly, 30L)
    }

    enum class PremiumType(@StringRes val labelRes: Int) {
        Monthly(R.string.settings_premium_upgrade_monthly),
        Yearly(R.string.settings_premium_upgrade_yearly),
    }

    @Serializable
    enum class Gender(@StringRes val labelRes: Int) {
        Male(R.string.settings_edit_profile_gender_male),
        Female(R.string.settings_edit_profile_gender_female),
        Unspecified(R.string.settings_edit_profile_gender_unspecified),
    }

    enum class ToastType(val stringId: Int, val length: Int = Toast.LENGTH_SHORT) {
        FailLogin(R.string.toast_fail_login),
        FailLogout(R.string.toast_fail_logout),
        FailSaveMood(R.string.toast_fail_save_mood),
        FailBackup(R.string.toast_fail_backup),
        FailRestore(R.string.toast_fail_restore),
        FailResetData(R.string.toast_fail_reset_data),
        FailSyncData(R.string.toast_fail_sync_data),
        SuccessLogin(R.string.toast_success_login),
        SuccessLogout(R.string.toast_success_logout),
        SuccessSaveMood(R.string.toast_success_save_mood),
        SuccessEditProfile(R.string.toast_success_save_profile),
        SuccessBackup(R.string.toast_success_backup),
        SuccessRestore(R.string.toast_success_restore),
        SuccessResetData(R.string.toast_success_reset_data),
        SuccessSyncData(R.string.toast_success_sync_data),
        MoodSaveLimit(R.string.toast_mood_save_limit),
    }

    enum class DialogType(
        val titleId: Int? = null,
        val textId: Int,
        val confirmButtonTextId: Int,
        val dismissButtonTextId: Int? = null,
    ) {
        None(
            textId = 0,
            confirmButtonTextId = 0,
        ),
        ErrorNetwork(
            textId = R.string.dialog_msg_error_network,
            confirmButtonTextId = R.string.common_ok,
        ),
        Logout(
            titleId = R.string.dialog_title_logout,
            textId = R.string.dialog_msg_logout,
            confirmButtonTextId = R.string.dialog_button_confirm_logout,
            dismissButtonTextId = R.string.dialog_button_dismiss_logout,
        ),
        ResetData(
            titleId = R.string.dialog_title_reset_data,
            textId = R.string.dialog_msg_reset_data,
            confirmButtonTextId = R.string.dialog_button_confirm_reset_data,
            dismissButtonTextId = R.string.dialog_button_dismiss_reset_data,
        ),
        Restore(
            titleId = R.string.dialog_title_restore,
            textId = R.string.dialog_msg_restore,
            confirmButtonTextId = R.string.dialog_button_confirm_restore,
            dismissButtonTextId = R.string.dialog_button_dismiss_restore,
        ),
        NotificationDenied(
            titleId = R.string.dialog_title_notification_denied,
            textId = R.string.dialog_msg_notification_denied,
            confirmButtonTextId = R.string.dialog_button_confirm_notification_denied,
            dismissButtonTextId = R.string.dialog_button_dismiss_notification_denied,
        ),
        ExactAlarmDenied(
            titleId = R.string.dialog_title_exact_alarm_denied,
            textId = R.string.dialog_msg_exact_alarm_denied,
            confirmButtonTextId = R.string.dialog_button_confirm_exact_alarm_denied,
            dismissButtonTextId = R.string.dialog_button_dismiss_exact_alarm_denied,
        ),
        BatteryOptimization(
            titleId = R.string.dialog_title_battery_optimization,
            textId = R.string.dialog_msg_battery_optimization,
            confirmButtonTextId = R.string.dialog_button_confirm_battery_optimization,
            dismissButtonTextId = R.string.dialog_button_dismiss_battery_optimization,
        ),
        Feedback(
            titleId = R.string.dialog_title_feedback,
            textId = R.string.dialog_msg_feedback,
            confirmButtonTextId = R.string.dialog_button_confirm_feedback,
            dismissButtonTextId = R.string.dialog_button_dismiss_feedback,
        ),
        AdsSaveMood(
            titleId = R.string.dialog_title_ads_save_mood,
            textId = R.string.dialog_msg_ads_save_mood,
            confirmButtonTextId = R.string.dialog_button_confirm_ads_save_mood,
            dismissButtonTextId = R.string.common_cancel,
        ),
        ForceUpdate(
            titleId = R.string.dialog_title_force_update,
            textId = R.string.dialog_msg_force_update,
            confirmButtonTextId = R.string.dialog_button_confirm_force_update,
        ),
        UnsavedProfile(
            titleId = R.string.dialog_title_unsaved_profile,
            textId = R.string.dialog_msg_unsaved_profile,
            confirmButtonTextId = R.string.dialog_button_confirm_unsaved_profile,
            dismissButtonTextId = R.string.dialog_button_dismiss_unsaved_profile,
        );

        @Composable
        fun confirmButtonColor(): Color {
            return when (this) {
                else -> Blue
            }
        }

        @Composable
        fun dismissButtonColor(): Color {
            return when (this) {
                else -> Red
            }
        }
    }
}