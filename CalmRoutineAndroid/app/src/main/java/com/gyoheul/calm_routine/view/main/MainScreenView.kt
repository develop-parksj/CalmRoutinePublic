package com.gyoheul.calm_routine.view.main

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.data.FirebaseModel
import com.gyoheul.calm_routine.model.ProfileData
import com.gyoheul.calm_routine.view.AlertDialogView
import com.gyoheul.calm_routine.view.LoadingScreenView
import com.gyoheul.calm_routine.view.home.HomeScreenView
import com.gyoheul.calm_routine.view.mood.MoodScreenView
import com.gyoheul.calm_routine.view.routine.RoutineScreenView
import com.gyoheul.calm_routine.view.settings.SettingsScreenView
import com.gyoheul.calm_routine.view.settings.SettingsScreenViewModel
import androidx.core.net.toUri

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    data object Home : BottomNavItem(
        "home",
        Icons.Default.Home,
        "Home"
    )
    data object Routine : BottomNavItem(
        "routine",
        Icons.Default.CheckCircle,
        "Routine"
    )
    data object Mood : BottomNavItem(
        "mood",
        Icons.Default.Mood,
        "Mood"
    )
    data object Settings : BottomNavItem(
        "settings",
        Icons.Default.Settings,
        "Settings"
    )
}

@Composable
fun MainScreenView(navController: NavHostController, settingsScreenViewModel: SettingsScreenViewModel, viewModel: MainScreenViewModel = viewModel()) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading
    val tabNavController = rememberNavController() // BottomNav専用
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Routine,
        BottomNavItem.Mood,
        BottomNavItem.Settings
    )

    LaunchedEffect(Unit) {
        viewModel.initialize()

        val profileData: ProfileData? = viewModel.preferencesManager.getSettingsProfileData()
        if (profileData == null) {
            navController.navigate("edit_profile")
        }
    }

    val firebaseConfigActivated by viewModel.firebaseConfigActivated

    if (firebaseConfigActivated && FirebaseModel.forceUpdate) {
        AlertDialogView(
            dialogType = EnumClass.DialogType.ForceUpdate,
            confirmButtonClick = {
                val appPackageName = context.packageName
                try {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "market://details?id=$appPackageName".toUri()
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                } catch (_: ActivityNotFoundException) {
                    // Play Storeアプリがない場合、ブラウザへフォールバック
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            "https://play.google.com/store/apps/details?id=$appPackageName".toUri()
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }
            },
            onDismissRequest = {}
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = tabNavController, items = items)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier
                .padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreenView(navController, tabNavController)
            }
            composable(BottomNavItem.Routine.route) {
                RoutineScreenView()
            }
            composable(BottomNavItem.Mood.route) {
                MoodScreenView(navController)
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreenView(navController, viewModel, settingsScreenViewModel)
            }
        }
    }

    if (isLoading) {
        LoadingScreenView()
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<BottomNavItem>
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val currentDestination = navController.currentBackStackEntryAsState().value?.destination

        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination?.route == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.label)
                },
                label = {
                    Text(text = item.label, fontSize = 12.sp)
                }
            )
        }
    }
}