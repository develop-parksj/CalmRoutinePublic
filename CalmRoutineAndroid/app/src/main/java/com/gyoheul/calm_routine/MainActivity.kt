package com.gyoheul.calm_routine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.common.Extension.channelId
import com.gyoheul.calm_routine.data.FirebaseModel
import com.gyoheul.calm_routine.data.NotificationModel
import com.gyoheul.calm_routine.data.SQLiteManager
import com.gyoheul.calm_routine.data.SharedPreferencesManager
import com.gyoheul.calm_routine.ui.theme.CalmRoutineTheme
import com.gyoheul.calm_routine.view.login.LoginScreenView
import com.gyoheul.calm_routine.view.main.MainScreenView
import com.gyoheul.calm_routine.view.moodHistory.MoodHistoryScreenView
import com.gyoheul.calm_routine.view.settings.SettingsScreenViewModel
import com.gyoheul.calm_routine.view.settings.editProfile.EditProfileScreenView
import com.gyoheul.calm_routine.view.settings.premiumUpgrade.PremiumUpgradeScreenView
import com.gyoheul.calm_routine.view.settings.syncData.SyncDataScreenView
import com.gyoheul.calm_routine.view.splash.SplashScreenView
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseModel.setSignInLauncher(this)
        createNotificationChannels(applicationContext)
        NotificationModel.setNotificationAlarm(application)

        val preferencesManager = SharedPreferencesManager(applicationContext)
        if (preferencesManager.getLastDeleteDataDate().isBefore(LocalDate.now())) {
            SQLiteManager(applicationContext).run {
                val targetDate = LocalDate.now().minusMonths(1)
                deleteRoutinesBeforeDate(targetDate)
                deleteMoodsBeforeDate(targetDate)
            }
            preferencesManager.setLastDeleteDataDate()
        }

        enableEdgeToEdge()
        setContent {
            val settingsScreenViewModel: SettingsScreenViewModel = viewModel()
            val appearanceUiState by settingsScreenViewModel.appearanceUiState.collectAsState()

            LaunchedEffect(Unit) {
                settingsScreenViewModel.loadAppearanceInfo()
            }

            CalmRoutineTheme(
                themeMode = appearanceUiState.themeMode,
                fontSize = appearanceUiState.fontSize,
                colorTheme = appearanceUiState.colorTheme,
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CalmRoutineApp(settingsScreenViewModel)
                }
            }
        }
    }

    private fun createNotificationChannels(context: Context) {
        val channels = listOf(
            NotificationChannel(
                EnumClass.NotificationType.Routine.channelId,
                context.getString(EnumClass.NotificationType.Routine.labelRes),
                NotificationManager.IMPORTANCE_HIGH
            ),
            NotificationChannel(
                EnumClass.NotificationType.Mood.channelId,
                context.getString(EnumClass.NotificationType.Mood.labelRes),
                NotificationManager.IMPORTANCE_HIGH
            ),
            NotificationChannel(
                EnumClass.NotificationType.AI.channelId,
                context.getString(EnumClass.NotificationType.AI.labelRes),
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        channels.forEach { manager.createNotificationChannel(it) }
    }

    @Composable
    fun CalmRoutineApp(settingsScreenViewModel: SettingsScreenViewModel) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "splash"
        ) {
            composable("splash") {
                SplashScreenView(navController)
            }
            composable("login") {
                LoginScreenView(navController)
            }
            composable("main") {
                MainScreenView(navController, settingsScreenViewModel)
            }
            composable("mood_history") {
                MoodHistoryScreenView(navController)
            }
            composable("edit_profile") {
                EditProfileScreenView(navController)
            }
            composable("sync_data") {
                SyncDataScreenView(navController)
            }
            composable("premium_upgrade") {
                PremiumUpgradeScreenView(navController)
            }
        }
    }
}