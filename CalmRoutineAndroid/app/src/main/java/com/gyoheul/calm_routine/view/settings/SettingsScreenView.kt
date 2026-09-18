package com.gyoheul.calm_routine.view.settings

import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.gyoheul.calm_routine.R
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.data.DateFormatModel
import com.gyoheul.calm_routine.data.ToastModel
import com.gyoheul.calm_routine.ui.theme.LocalMainColorScheme
import com.gyoheul.calm_routine.ui.theme.LocalMainTypography
import com.gyoheul.calm_routine.ui.theme.LocalPremiumColor
import com.gyoheul.calm_routine.view.AlertDialogView
import com.gyoheul.calm_routine.view.LoadingScreenView
import com.gyoheul.calm_routine.view.ShadowBox
import com.gyoheul.calm_routine.view.main.MainScreenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalTime
import androidx.core.net.toUri

@Composable
fun SettingsScreenView(
    navHostController: NavHostController,
    mainViewModel: MainScreenViewModel = viewModel(),
    viewModel: SettingsScreenViewModel = viewModel()
) {
    val isLoading by viewModel.isLoading

    val accountUiState by viewModel.accountUiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // プロフィール情報の表示
        Text(
            text = "${accountUiState.userName} (${accountUiState.email})",
            style = LocalMainTypography.current.titleMedium,
            modifier = Modifier.padding(16.dp)
        )

        HorizontalDivider()

        ShadowBox {
            LazyColumn (
                modifier = Modifier.weight(1f)
            ) {
                item {
                    AccountSettingsScreen(navHostController)
                }

                item {
                    NotificationSettingsScreen(viewModel)
                }

                item {
                    AICommentSettingsScreen(navHostController, viewModel)
                }

                item {
                    AppearanceSettingsScreen(viewModel)
                }

                item {
                    DataSettingsScreen(navHostController, mainViewModel, viewModel)
                }

                item {
                    PremiumSettingsScreen(navHostController, viewModel)
                }

                item {
                    OtherSettingsScreen(viewModel)
                }

                item {
                    WarningSettingsScreen(navHostController, mainViewModel, viewModel)
                }
            }
        }
    }

    if (isLoading) {
        LoadingScreenView()
    }
}

@Composable
fun AccountSettingsScreen(
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // プロフィール編集ボタン
        SettingButtonItem(
            title = stringResource(id = R.string.settings_edit_profile),
            description = stringResource(id = R.string.settings_edit_profile_desc),
            onClick = {
                navController.navigate("edit_profile")
            }
        )
    }
}

@Composable
fun NotificationSettingsScreen(
    viewModel: SettingsScreenViewModel = viewModel()
) {
    val context = LocalContext.current

    val uiState by viewModel.notificationUiState.collectAsState()

    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        val currentTime = uiState.notificationTime
        TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
                viewModel.setNotificationTime(LocalTime.of(hour, minute))
                showTimePicker = false
            },
            currentTime.hour,
            currentTime.minute,
            true
        ).apply {
            setOnCancelListener { showTimePicker = false }
        }.show()
    }

    var showTypeDialog by remember { mutableStateOf(false) }

    if (showTypeDialog) {
        NotificationTypeDialog(
            selected = uiState.selectedTypes,
            onDismiss = { showTypeDialog = false },
            onConfirm = {
                viewModel.setNotificationTypes(it)
                showTypeDialog = false
            }
        )
    }

    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf(EnumClass.DialogType.None) }

    if (showPermissionDeniedDialog) {
        val confirmButtonClick: (() -> Unit)? =
            when (dialogType) {
                EnumClass.DialogType.NotificationDenied -> {
                    {
                        val intent = Intent().apply {
                            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                }
                EnumClass.DialogType.ExactAlarmDenied -> {
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent().apply {
                                action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                                data = "package:${context.packageName}".toUri()
                            }
                            context.startActivity(intent)
                        }
                    }
                }
                else -> null
            }
        AlertDialogView(
            dialogType = dialogType,
            confirmButtonClick = confirmButtonClick,
            onDismissRequest = {
                showPermissionDeniedDialog = false
            }
        )
    }

    var showNotificationPermission by remember { mutableStateOf(false) }

    if (showNotificationPermission) {
        NotificationPermissionHandler(
            onResult = { status ->
                when (status) {
                    PermissionStatus.AllGranted -> {
                        viewModel.toggleNotifications(true)
                    }
                    PermissionStatus.NotificationDenied -> {
                        dialogType = EnumClass.DialogType.NotificationDenied
                        showPermissionDeniedDialog = true
                    }
                    PermissionStatus.ExactAlarmDenied -> {
                        dialogType = EnumClass.DialogType.ExactAlarmDenied
                        showPermissionDeniedDialog = true
                    }
                }
                showNotificationPermission = false
            }
        )
    }

    if (uiState.showBatteryOptimizationDialog) {
        AlertDialogView(
            dialogType = EnumClass.DialogType.BatteryOptimization,
            confirmButtonClick = {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                context.startActivity(intent)
            },
            onDismissRequest = {
                viewModel.onBatteryOptimizationDialogDismiss()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        // プッシュ通知のオン/オフ
        SettingSwitchItem(
            title = stringResource(R.string.settings_push_notifications),
            description = stringResource(R.string.settings_push_notifications_desc),
            checked = uiState.notificationsEnabled,
            onCheckedChange = {
                if (it) {
                    showNotificationPermission = true
                } else {
                    viewModel.toggleNotifications(false)
                }
            }
        )

        Spacer(modifier = Modifier.height(3.dp))

        // 通知時間の文字設定
        SettingButtonItem(
            title = stringResource(R.string.settings_notification_time),
            description = uiState.notificationTime.format(DateFormatModel.notificationTimeFormat),
            onClick = {
                showTimePicker = true
            }
        )

        Spacer(modifier = Modifier.height(3.dp))

        // 通知タイプ
        SettingButtonItem(
            title = stringResource(R.string.settings_notification_type),
            description = uiState.selectedTypes.map { stringResource(it.labelRes) }.joinToString(", "),
            onClick = {
                showTypeDialog = true
            }
        )
    }
}

@Composable
fun AICommentSettingsScreen(
    navController: NavHostController,
    viewModel: SettingsScreenViewModel = viewModel()
) {
    val uiState by viewModel.aiCommentUiState.collectAsState()

    var showStyleDialog by remember { mutableStateOf(false) }

    if (showStyleDialog) {
        AICommentStyleDialog(
            selected = uiState.style,
            onDismiss = { showStyleDialog = false },
            onConfirm = {
                viewModel.setAICommentStyle(it)
                showStyleDialog = false
            }
        )
    }

    var showFrequencyDialog by remember { mutableStateOf(false) }

    if (showFrequencyDialog) {
        AICommentFrequencyDialog(
            selected = uiState.frequency,
            onDismiss = { showFrequencyDialog = false },
            onConfirm = {
                viewModel.setAICommentFrequency(it)
                showFrequencyDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        // AIコメントスタイル
        PremiumSettingButtonItem(
            navController = navController,
            title = stringResource(R.string.settings_ai_comment_style),
            description = stringResource(uiState.style.labelRes),
            onClick = {
                showStyleDialog = true
            }
        )

        Spacer(modifier = Modifier.height(3.dp))

        // AIコメント頻度
        PremiumSettingButtonItem(
            navController = navController,
            title = stringResource(R.string.settings_ai_comment_frequency),
            description = stringResource(uiState.frequency.labelRes),
            onClick = {
                showFrequencyDialog = true
            }
        )
    }
}

@Composable
fun AppearanceSettingsScreen(
    viewModel: SettingsScreenViewModel = viewModel()
) {
    val uiState by viewModel.appearanceUiState.collectAsState()

    var showThemeModeDialog by remember { mutableStateOf(false) }

    if (showThemeModeDialog) {
        AppearanceThemeModeDialog(
            selected = uiState.themeMode,
            onDismiss = { showThemeModeDialog = false },
            onConfirm = {
                viewModel.setAppearanceThemeMode(it)
                showThemeModeDialog = false
            }
        )
    }

    var showFontSizeDialog by remember { mutableStateOf(false) }

    if (showFontSizeDialog) {
        AppearanceFontSizeDialog(
            selected = uiState.fontSize,
            onDismiss = { showFontSizeDialog = false },
            onConfirm = {
                viewModel.setAppearanceFontSize(it)
                showFontSizeDialog = false
            }
        )
    }

    var showColorThemeDialog by remember { mutableStateOf(false) }

    if (showColorThemeDialog) {
        AppearanceColorThemeDialog(
            selected = uiState.colorTheme,
            onDismiss = { showColorThemeDialog = false },
            onConfirm = {
                viewModel.setAppearanceColorTheme(it)
                showColorThemeDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        // テーマモード
        SettingButtonItem(
            title = stringResource(R.string.settings_theme_mode),
            description = stringResource(uiState.themeMode.labelRes),
            onClick = {
                showThemeModeDialog = true
            }
        )

        Spacer(modifier = Modifier.height(3.dp))

        // フォントサイズ
        SettingButtonItem(
            title = stringResource(R.string.settings_font_size),
            description = stringResource(uiState.fontSize.labelRes),
            onClick = {
                showFontSizeDialog = true
            }
        )

        Spacer(modifier = Modifier.height(3.dp))

        // カラーテーマ
        SettingButtonItem(
            title = stringResource(R.string.settings_color_theme),
            description = stringResource(uiState.colorTheme.labelRes),
            onClick = {
                showColorThemeDialog = true
            }
        )
    }
}

@Composable
fun DataSettingsScreen(
    navController: NavHostController,
    mainViewModel: MainScreenViewModel = viewModel(),
    viewModel: SettingsScreenViewModel = viewModel()
) {
    val context = LocalContext.current

    var showRestoreDialog by remember { mutableStateOf(false) }

    if (showRestoreDialog) {
        AlertDialogView(
            dialogType = EnumClass.DialogType.Restore,
            dismissButtonClick = {
                mainViewModel.showLoading()
                CoroutineScope(Dispatchers.IO).launch {
                    val result = viewModel.restoreUserData()
                    CoroutineScope(Dispatchers.Main).launch {
                        mainViewModel.dismissLoading()
                        ToastModel.showToast(
                            context,
                            if (result) {
                                EnumClass.ToastType.SuccessRestore
                            } else {
                                EnumClass.ToastType.FailRestore
                            }
                        )
                    }
                }
            },
            onDismissRequest =  {
                showRestoreDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        // データバックアップ
        PremiumSettingButtonItem(
            navController = navController,
            title = stringResource(R.string.settings_data_backup),
            description = stringResource(R.string.settings_data_backup_desc),
            warning = stringResource(R.string.settings_data_backup_warning),
            onClick = {
                mainViewModel.showLoading()
                CoroutineScope(Dispatchers.IO).launch {
                    val result = viewModel.backupUserData()
                    CoroutineScope(Dispatchers.Main).launch {
                        mainViewModel.dismissLoading()
                        ToastModel.showToast(
                            context,
                            if (result) {
                                EnumClass.ToastType.SuccessBackup
                            } else {
                                EnumClass.ToastType.FailBackup
                            }
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(3.dp))

        // データ復元
        PremiumSettingButtonItem(
            navController = navController,
            title = stringResource(R.string.settings_data_restore),
            description = stringResource(R.string.settings_data_restore_desc),
            onClick = {
                showRestoreDialog = true
            }
        )

        Spacer(modifier = Modifier.height(3.dp))

        // データ同期
        PremiumSettingButtonItem(
            navController = navController,
            title = stringResource(R.string.settings_data_sync),
            description = stringResource(R.string.settings_data_sync_desc),
            onClick = {
                navController.navigate("sync_data")
            }
        )
    }
}

@Composable
fun PremiumSettingsScreen(
    navController: NavHostController,
    viewModel: SettingsScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val isPremium by viewModel.isPremium.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        // 定期購読の誘導
        if (!isPremium) {
            SettingButtonItem(
                title = stringResource(R.string.settings_premium_upgrade),
                description = stringResource(R.string.settings_premium_upgrade_desc),
                onClick = {
                    navController.navigate("premium_upgrade")
                }
            )

            Spacer(modifier = Modifier.height(3.dp))
        }

        // 定期購読の状態
        SettingButtonItem(
            title = stringResource(R.string.settings_subscription_status),
            description = stringResource(
                if (!isPremium) {
                    R.string.settings_subscription_status_free_plan
                } else {
                    R.string.settings_subscription_status_premium_active
                }
            )
        )

        if (isPremium) {
            Spacer(modifier = Modifier.height(3.dp))

            // 定期購読の管理
            SettingButtonItem(
                title = stringResource(R.string.settings_manage_subscription),
                description = stringResource(R.string.settings_manage_subscription_desc),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = "https://play.google.com/store/account/subscriptions".toUri()
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun OtherSettingsScreen(
    viewModel: SettingsScreenViewModel = viewModel()
) {
    val context = LocalContext.current

    val uiState by viewModel.otherUiState.collectAsState()

    var showFeedbackDialog by remember { mutableStateOf(false) }

    if (showFeedbackDialog) {
        AlertDialogView(
            dialogType = EnumClass.DialogType.Feedback,
            confirmButtonClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822" // メールMIMEタイプ
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("park.sj.develop@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "CalmRoutine Feedback")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        """
                            Hi CalmRoutine team 🌿
        
                            I wanted to share some thoughts...
        
        
        
                            ——
                            Firebase email: ${viewModel.user.value?.email}
                            Firebase uid: ${viewModel.user.value?.uid}
                            App version: ${uiState.version}
                            Device: ${Build.MODEL} (${Build.VERSION.RELEASE})
                        """.trimIndent()
                    )
                }
                context.startActivity(Intent.createChooser(intent, "Send Feedback"))
            },
            onDismissRequest = {
                showFeedbackDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        // ライセンス
        val ossLicenseTitle = stringResource(R.string.settings_open_source_licenses)
        SettingButtonItem(
            title = ossLicenseTitle,
            description = stringResource(R.string.settings_open_source_licenses_desc),
            onClick = {
                val intent = Intent(context, OssLicensesMenuActivity::class.java)
                OssLicensesMenuActivity.setActivityTitle(ossLicenseTitle)
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(3.dp))

        // プライバシーポリシー
        SettingButtonItem(
            title = stringResource(R.string.settings_privacy_policy),
            description = stringResource(R.string.settings_privacy_policy_desc),
            onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://gyoheulweb.web.app/privacy-policy".toUri()
                )
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(3.dp))

        // バージョン
        SettingButtonItem(
            title = stringResource(R.string.settings_version_info),
            description = stringResource(R.string.settings_version_info_desc, uiState.version),
        )

        Spacer(modifier = Modifier.height(3.dp))

        // フィードバック
        SettingButtonItem(
            title = stringResource(R.string.settings_feedback),
            description = stringResource(R.string.settings_feedback_desc),
            onClick = {
                showFeedbackDialog = true
            }
        )
    }
}

@Composable
fun WarningSettingsScreen(
    navHostController: NavHostController,
    mainViewModel: MainScreenViewModel = viewModel(),
    viewModel: SettingsScreenViewModel = viewModel()
) {
    val context = LocalContext.current

    var dialogType by remember { mutableStateOf(EnumClass.DialogType.None) }
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        val dismissButtonClick: (() -> Unit)? =
            when (dialogType) {
                EnumClass.DialogType.Logout -> {
                    {
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = viewModel.logout()
                            CoroutineScope(Dispatchers.Main).launch {
                                ToastModel.showToast(
                                    context,
                                    if (result) {
                                        navHostController.navigate("login") {
                                            popUpTo("main") { inclusive = true }
                                            launchSingleTop = true
                                        }

                                        EnumClass.ToastType.SuccessLogout
                                    } else {
                                        EnumClass.ToastType.FailLogout
                                    }
                                )
                            }
                        }
                    }
                }
                EnumClass.DialogType.ResetData -> {
                    {
                        mainViewModel.showLoading()
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = viewModel.resetData()
                            CoroutineScope(Dispatchers.Main).launch {
                                mainViewModel.dismissLoading()
                                ToastModel.showToast(
                                    context,
                                    if (result) {
                                        EnumClass.ToastType.SuccessResetData
                                    } else {
                                        EnumClass.ToastType.FailResetData
                                    }
                                )
                            }
                        }
                    }
                }
                else -> null
            }
        AlertDialogView(
            dialogType = dialogType,
            dismissButtonClick = dismissButtonClick,
            onDismissRequest =  {
                dialogType = EnumClass.DialogType.None
                showDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        // データリセット
        WarningSettingButtonItem(
            title = stringResource(R.string.settings_data_reset),
            description = stringResource(R.string.settings_data_reset_desc),
            onClick = {
                dialogType = EnumClass.DialogType.ResetData
                showDialog = true
            }
        )

        Spacer(modifier = Modifier.height(3.dp))

        // ログアウトボタン (重要なので区別)
        WarningSettingButtonItem(
            title = stringResource(id = R.string.settings_logout),
            description = stringResource(id = R.string.settings_logout_desc),
            onClick = {
                dialogType = EnumClass.DialogType.Logout
                showDialog = true
            }
        )
    }
}

@Composable
fun SettingButtonItem(
    title: String,
    description: String,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .then(
                onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = LocalMainColorScheme.current.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = LocalMainTypography.current.bodyLarge,
                color = LocalMainColorScheme.current.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = LocalMainTypography.current.bodySmall,
                color = LocalMainColorScheme.current.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun WarningSettingButtonItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = LocalMainColorScheme.current.error.copy(alpha = 0.1f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = LocalMainTypography.current.bodyLarge,
                color = LocalMainColorScheme.current.error,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = LocalMainTypography.current.bodySmall,
                color = LocalMainColorScheme.current.error.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun PremiumSettingButtonItem(
    navController: NavHostController,
    title: String,
    description: String,
    warning: String = "",
    viewModel: SettingsScreenViewModel = viewModel(),
    onClick: () -> Unit
) {
    val isPremium by viewModel.isPremium.collectAsState()

    var showPremiumUpgradeDialog by remember { mutableStateOf(false) }

    if (showPremiumUpgradeDialog) {
        PremiumUpgradeDialog(
            onConfirm = {
                navController.navigate("premium_upgrade")
            },
            onDismiss =  {
                showPremiumUpgradeDialog = false
            }
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(
                onClick = {
                    if (!isPremium) {
                        showPremiumUpgradeDialog = true
                    } else {
                        onClick()
                    }
                }
            ),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = LocalPremiumColor.current.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = LocalMainTypography.current.bodyLarge,
                color = LocalPremiumColor.current.text,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = LocalMainTypography.current.bodySmall,
                color = LocalPremiumColor.current.text.copy(alpha = 0.7f)
            )
            if (warning.isNotBlank()) {
                Text(
                    text = warning,
                    style = LocalMainTypography.current.bodySmall,
                    color = LocalMainColorScheme.current.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = LocalMainColorScheme.current.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = LocalMainTypography.current.bodyLarge,
                    color = LocalMainColorScheme.current.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = LocalMainTypography.current.bodySmall,
                    color = LocalMainColorScheme.current.onSurface.copy(alpha = 0.6f)
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

sealed class PermissionStatus {
    data object AllGranted : PermissionStatus()
    data object NotificationDenied : PermissionStatus()
    data object ExactAlarmDenied : PermissionStatus()
}

@Composable
fun NotificationPermissionHandler(
    onResult: (PermissionStatus) -> Unit,
) {
    val context = LocalContext.current

    fun checkExactAlarmPermission(context: Context, onResult: (PermissionStatus) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                onResult(PermissionStatus.ExactAlarmDenied)
                return
            }
        }
        onResult(PermissionStatus.AllGranted)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkExactAlarmPermission(context, onResult)
        } else {
            onResult(PermissionStatus.NotificationDenied)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                return@LaunchedEffect
            }
        }
        checkExactAlarmPermission(context, onResult)
    }
}

@Composable
fun NotificationTypeDialog(
    selected: Set<EnumClass.NotificationType>,
    onDismiss: () -> Unit,
    onConfirm: (Set<EnumClass.NotificationType>) -> Unit
) {
    val tempSelected = remember { mutableStateListOf<EnumClass.NotificationType>().apply { addAll(selected) } }

    AlertDialogView(
        title = stringResource(R.string.settings_notification_type_dialog_title),
        text = {
            Column {
                EnumClass.NotificationType.entries.forEach { type ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (tempSelected.contains(type)) {
                                    tempSelected.remove(type)
                                } else {
                                    tempSelected.add(type)
                                }
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = tempSelected.contains(type),
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(type.labelRes))
                    }
                }
            }
        },
        confirmButtonText = stringResource(R.string.common_ok),
        confirmButtonClick = {
            onConfirm(tempSelected.toSet())
        },
        dismissButtonText = stringResource(R.string.common_cancel),
        onDismissRequest = onDismiss
    )
}

@Composable
fun AICommentStyleDialog(
    selected: EnumClass.AICommentStyle,
    onDismiss: () -> Unit,
    onConfirm: (EnumClass.AICommentStyle) -> Unit
) {
    var current by remember { mutableStateOf(selected) }

    AlertDialogView(
        title = stringResource(R.string.settings_ai_comment_dialog_title),
        text = {
            Column {
                EnumClass.AICommentStyle.entries.forEach { style ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { current = style }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == style, onClick = { current = style })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(style.labelRes))
                    }
                }
            }
        },
        confirmButtonText = stringResource(R.string.common_ok),
        confirmButtonClick = {
            onConfirm(current)
        },
        dismissButtonText = stringResource(R.string.common_cancel),
        onDismissRequest = onDismiss
    )
}

@Composable
fun AICommentFrequencyDialog(
    selected: EnumClass.AICommentFrequency,
    onDismiss: () -> Unit,
    onConfirm: (EnumClass.AICommentFrequency) -> Unit
) {
    var current by remember { mutableStateOf(selected) }

    AlertDialogView(
        title = stringResource(R.string.settings_ai_frequency_dialog_title),
        text = {
            Column {
                EnumClass.AICommentFrequency.entries.forEach { style ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { current = style }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == style, onClick = { current = style })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(style.labelRes))
                    }
                }
            }
        },
        confirmButtonText = stringResource(R.string.common_ok),
        confirmButtonClick = {
            onConfirm(current)
        },
        dismissButtonText = stringResource(R.string.common_cancel),
        onDismissRequest = onDismiss
    )
}

@Composable
fun AppearanceThemeModeDialog(
    selected: EnumClass.ThemeMode,
    onDismiss: () -> Unit,
    onConfirm: (EnumClass.ThemeMode) -> Unit
) {
    var current by remember { mutableStateOf(selected) }

    AlertDialogView(
        title = stringResource(R.string.settings_theme_mode_dialog_title),
        text = {
            Column {
                EnumClass.ThemeMode.entries.forEach { style ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { current = style }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == style, onClick = { current = style })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(style.labelRes))
                    }
                }
            }
        },
        confirmButtonText = stringResource(R.string.common_ok),
        confirmButtonClick = {
            onConfirm(current)
        },
        dismissButtonText = stringResource(R.string.common_cancel),
        onDismissRequest = onDismiss
    )
}

@Composable
fun AppearanceFontSizeDialog(
    selected: EnumClass.FontSize,
    onDismiss: () -> Unit,
    onConfirm: (EnumClass.FontSize) -> Unit
) {
    var current by remember { mutableStateOf(selected) }

    AlertDialogView(
        title = stringResource(R.string.settings_font_size_dialog_title),
        text = {
            Column {
                EnumClass.FontSize.entries.forEach { style ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { current = style }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == style, onClick = { current = style })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(style.labelRes))
                    }
                }
            }
        },
        confirmButtonText = stringResource(R.string.common_ok),
        confirmButtonClick = {
            onConfirm(current)
        },
        dismissButtonText = stringResource(R.string.common_cancel),
        onDismissRequest = onDismiss
    )
}

@Composable
fun AppearanceColorThemeDialog(
    selected: EnumClass.ColorTheme,
    onDismiss: () -> Unit,
    onConfirm: (EnumClass.ColorTheme) -> Unit
) {
    var current by remember { mutableStateOf(selected) }

    AlertDialogView(
        title = stringResource(R.string.settings_color_theme_dialog_title),
        text = {
            Column {
                EnumClass.ColorTheme.entries.forEach { style ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { current = style }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = current == style, onClick = { current = style })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(style.labelRes))
                    }
                }
            }
        },
        confirmButtonText = stringResource(R.string.common_ok),
        confirmButtonClick = {
            onConfirm(current)
        },
        dismissButtonText = stringResource(R.string.common_cancel),
        onDismissRequest = onDismiss
    )
}

@Composable
fun PremiumUpgradeDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialogView(
        title = stringResource(R.string.settings_premium_upgrade_dialog_title),
        text = {
            Column {
                Text(
                    text = stringResource(R.string.settings_premium_upgrade_dialog_desc_locked_feature),
                    style = LocalMainTypography.current.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                listOf(
                    R.string.settings_premium_upgrade_benefit1_title,
                    R.string.settings_premium_upgrade_benefit2_title,
                    R.string.settings_premium_upgrade_benefit3_title,
                    R.string.settings_premium_upgrade_benefit4_title,
                    R.string.settings_premium_upgrade_benefit5_title,
                    R.string.settings_premium_upgrade_benefit6_title,
                ).forEach { labelRes ->
                    Text(
                        text = "・${stringResource(labelRes)}",
                        modifier = Modifier
                            .padding(
                                vertical = 2.dp,
                                horizontal = 5.dp
                            )
                    )
                }
            }
        },
        confirmButtonText = stringResource(R.string.settings_premium_upgrade_dialog_button),
        confirmButtonClick = {
            onConfirm()
        },
        dismissButtonText = stringResource(R.string.common_cancel),
        onDismissRequest = onDismiss
    )
}