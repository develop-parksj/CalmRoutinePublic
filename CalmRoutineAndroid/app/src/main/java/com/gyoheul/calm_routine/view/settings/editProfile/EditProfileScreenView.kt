package com.gyoheul.calm_routine.view.settings.editProfile

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.gyoheul.calm_routine.R
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.data.DateFormatModel
import com.gyoheul.calm_routine.data.ToastModel
import com.gyoheul.calm_routine.ui.theme.LocalMainColorScheme
import com.gyoheul.calm_routine.ui.theme.LocalTextColor
import com.gyoheul.calm_routine.view.AlertDialogView
import com.gyoheul.calm_routine.view.DefaultButton
import com.gyoheul.calm_routine.view.DefaultTopAppBar
import com.gyoheul.calm_routine.view.LoadingScreenView
import com.gyoheul.calm_routine.view.NativeBottomAdView
import java.time.LocalDate

@Composable
fun EditProfileScreenView(navController: NavHostController, viewModel: EditProfileScreenViewModel = viewModel()) {
    val context = LocalContext.current
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

    var showUnsavedProfileDialog by remember { mutableStateOf(false) }

    if (showUnsavedProfileDialog) {
        AlertDialogView(
            dialogType = EnumClass.DialogType.UnsavedProfile,
            confirmButtonClick = {
                viewModel.onSaveClick()
                navController.popBackStack()
                ToastModel.showToast(context, EnumClass.ToastType.SuccessEditProfile)
            },
            dismissButtonClick = {
                navController.popBackStack()
            },
            onDismissRequest = {
                showUnsavedProfileDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            DefaultTopAppBar(
                title = stringResource(id = R.string.settings_edit_profile_title),
                onNavigationClick = {
                    val result = viewModel.onReturnClick()
                    if (result) {
                        navController.popBackStack()
                    } else {
                        showUnsavedProfileDialog = true
                    }
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
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // メール (読み取り専用)
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = {},
                    label = {
                        Text(stringResource(R.string.settings_edit_profile_email_label))
                    },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LocalMainColorScheme.current.primary,
                        unfocusedBorderColor = LocalMainColorScheme.current.primary,
                        cursorColor = LocalTextColor.current
                    )
                )

                // 1. ニックネームの修正
                OutlinedTextField(
                    value = uiState.nickname,
                    onValueChange = viewModel::onNicknameChange,
                    label = {
                        Text(stringResource(R.string.settings_edit_profile_nickname_label))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LocalMainColorScheme.current.primary,
                        unfocusedBorderColor = LocalMainColorScheme.current.primary,
                        cursorColor = LocalTextColor.current
                    )
                )

                // 2. 性別の選択 (Segmented Control)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EnumClass.Gender.entries.forEach { gender ->
                        val isSelected = uiState.gender == gender
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onGenderChange(gender) },
                            label = {
                                Text(
                                    text = stringResource(gender.labelRes),
                                    color = if (isSelected) LocalMainColorScheme.current.onPrimary else LocalTextColor.current
                                )
                            },
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = LocalMainColorScheme.current.primary
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LocalMainColorScheme.current.primary.copy(alpha = 0.5f),
                                labelColor = LocalTextColor.current,
                            )
                        )
                    }
                }

                // 3. 誕生日の選択 (デフォルト 2000-01-01, アイコン込み)
                val datePickerDialog = remember {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            viewModel.onBirthDateChange(LocalDate.of(year, month + 1, day))
                        },
                        uiState.birthDate?.year ?: 2000,
                        (uiState.birthDate?.monthValue ?: 1) - 1,
                        uiState.birthDate?.dayOfMonth ?: 1
                    )
                }

                OutlinedTextField(
                    value = uiState.birthDate?.format(DateFormatModel.birthDateFormat) ?: "----.--.--",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.settings_edit_profile_birthdate_label)) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select Date",
                            tint = LocalMainColorScheme.current.onPrimary,
                            modifier = Modifier.clickable { datePickerDialog.show() }
                        )
                    },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LocalMainColorScheme.current.primary,
                        unfocusedBorderColor = LocalMainColorScheme.current.primary,
                        cursorColor = LocalTextColor.current
                    )
                )

                // 4. 興味・関心の入力 + 追加ボタン
                val addEnable = uiState.interests.size < 5 && uiState.interestInput.isNotBlank()
                OutlinedTextField(
                    value = uiState.interestInput,
                    onValueChange = viewModel::onInterestInputChange,
                    label = { Text(stringResource(R.string.settings_edit_profile_interest_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.onAddInterest() }
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = { viewModel.onAddInterest() },
                            enabled = addEnable
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = if (addEnable) LocalMainColorScheme.current.onPrimary else LocalTextColor.current
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // 興味・関心のタグリスト
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.interests.forEach { interest ->
                        AssistChip(
                            label = {
                                Text(
                                    text = interest,
                                    color = LocalTextColor.current
                                )
                            },
                            onClick = {},
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = LocalMainColorScheme.current.primary,
                                borderWidth = 2.dp
                            ),
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Delete",
                                    tint = LocalMainColorScheme.current.onPrimary,
                                    modifier = Modifier.clickable { viewModel.onRemoveInterest(interest) }
                                )
                            }
                        )
                    }
                }

                DefaultButton(
                    onClick = {
                        viewModel.onSaveClick()
                        navController.popBackStack()
                        ToastModel.showToast(context, EnumClass.ToastType.SuccessEditProfile)
                    },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.settings_edit_profile_save_button))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            NativeBottomAdView(nativeBottomAd)
        }
    }

    if (isLoading) {
        LoadingScreenView()
    }
}