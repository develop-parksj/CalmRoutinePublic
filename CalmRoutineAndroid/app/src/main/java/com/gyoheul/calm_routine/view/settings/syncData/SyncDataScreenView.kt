package com.gyoheul.calm_routine.view.settings.syncData

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.gyoheul.calm_routine.R
import com.gyoheul.calm_routine.ui.theme.LocalMainColorScheme
import com.gyoheul.calm_routine.ui.theme.LocalMainTypography
import com.gyoheul.calm_routine.view.DefaultTextButton
import com.gyoheul.calm_routine.view.DefaultTopAppBar
import com.gyoheul.calm_routine.view.LoadingScreenView
import com.gyoheul.calm_routine.view.NativeBottomAdView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncDataScreenView(navController: NavHostController, viewModel: SyncDataScreenViewModel = viewModel()) {
    val isLoading by viewModel.isLoading
    val nativeBottomAd by viewModel.nativeBottomAd.collectAsState()

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    DisposableEffect(nativeBottomAd) {
        onDispose {
            nativeBottomAd?.destroy()
        }
    }

    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = stringResource(id = R.string.settings_sync_data_title),
                onNavigationClick = {
                    navController.popBackStack()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                // 自動バックアップのトグル
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.settings_sync_data_auto_backup),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = uiState.autoBackupEnabled,
                        onCheckedChange = { viewModel.onAutoBackupEnabledClick(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.autoBackupEnabled) {
                    // バックアップ周期の選択
                    ExposedDropdownMenuBox(
                        expanded = uiState.isFrequencyMenuExpanded,
                        onExpandedChange = {
                            viewModel.setFrequencyMenuExpanded(!uiState.isFrequencyMenuExpanded)
                        }
                    ) {
                        TextField(
                            readOnly = true,
                            value = stringResource(uiState.backupFrequency.labelRes),
                            onValueChange = {}, // 読み取り専用のため空の値
                            label = { Text(stringResource(R.string.settings_sync_data_backup_frequency)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.isFrequencyMenuExpanded)
                            },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(
                                focusedContainerColor = LocalMainColorScheme.current.primary,
                                unfocusedContainerColor = LocalMainColorScheme.current.primary,
                                focusedTextColor = LocalMainColorScheme.current.onPrimary,
                                unfocusedTextColor = LocalMainColorScheme.current.onPrimary
                            ),
                            modifier = Modifier
                                .menuAnchor(
                                    type = ExposedDropdownMenuAnchorType.PrimaryEditable,
                                    enabled = true
                                )
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = uiState.isFrequencyMenuExpanded,
                            onDismissRequest = { viewModel.setFrequencyMenuExpanded(false) }
                        ) {
                            uiState.backupFrequencyOptions.forEach { freq ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(freq.labelRes)) },
                                    onClick = {
                                        viewModel.onBackupFrequencyClick(freq)
                                        viewModel.setFrequencyMenuExpanded(false)
                                    }
                                )
                            }
                        }
                    }
                    Text(
                        text = stringResource(R.string.settings_sync_data_backup_frequency_desc),
                        style = LocalMainTypography.current.bodySmall,
                        color = LocalMainColorScheme.current.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // 最終同期時間の表示
                Text(
                    text = "${stringResource(R.string.settings_sync_data_last_sync)}: ${uiState.lastSyncTime ?: "–"}",
                    style = LocalMainTypography.current.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 手動同期ボタン
                DefaultTextButton(
                    title = stringResource(R.string.settings_sync_data_manual_sync),
                    onClick = { viewModel.onManualSyncClick() },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = stringResource(R.string.settings_sync_data_manual_sync_desc),
                    style = LocalMainTypography.current.bodySmall,
                    color = LocalMainColorScheme.current.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            NativeBottomAdView(nativeBottomAd)
        }
    }

    if (isLoading) {
        LoadingScreenView()
    }
}