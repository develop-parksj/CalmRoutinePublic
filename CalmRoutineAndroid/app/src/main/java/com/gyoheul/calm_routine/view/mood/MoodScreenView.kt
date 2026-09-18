package com.gyoheul.calm_routine.view.mood

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.play.core.review.ReviewManagerFactory
import com.gyoheul.calm_routine.R
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.data.LogModel
import com.gyoheul.calm_routine.data.MobileAdsModel
import com.gyoheul.calm_routine.data.ToastModel
import com.gyoheul.calm_routine.model.MoodEmotion
import com.gyoheul.calm_routine.ui.theme.Blue
import com.gyoheul.calm_routine.ui.theme.LocalMainColorScheme
import com.gyoheul.calm_routine.ui.theme.LocalMainTypography
import com.gyoheul.calm_routine.ui.theme.LocalPremiumColor
import com.gyoheul.calm_routine.ui.theme.LocalTextColor
import com.gyoheul.calm_routine.ui.theme.Red
import com.gyoheul.calm_routine.view.AlertDialogView
import com.gyoheul.calm_routine.view.DefaultButton
import com.gyoheul.calm_routine.view.LoadingScreenView
import com.gyoheul.calm_routine.view.settings.PremiumUpgradeDialog

@Composable
fun MoodScreenView(navController: NavHostController, viewModel: MoodScreenViewModel = viewModel()) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading
    val isPremium by viewModel.isPremium.collectAsState()
    val rewardedAdLoaded by viewModel.rewardedAdLoaded

    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    val emotionUiState by viewModel.emotionUiState.collectAsState()

    var showAddCustomMoodDialog by remember { mutableStateOf(false) }

    if (showAddCustomMoodDialog) {
        AddCustomMoodDialog(
            onDismiss = { showAddCustomMoodDialog = false },
            onConfirm = { customMood ->
                viewModel.addCustomMood(customMood)
            }
        )
    }

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

    var showAdsSaveMoodDialog by remember { mutableStateOf(false) }

    if (showAdsSaveMoodDialog) {
        AlertDialogView(
            dialogType = EnumClass.DialogType.AdsSaveMood,
            confirmButtonClick = {
                MobileAdsModel.showRewardedAd(context as Activity) {
                    viewModel.saveMood()

                    val manager = ReviewManagerFactory.create(context)
                    val request = manager.requestReviewFlow()

                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // レビューフロー開始
                            val reviewInfo = task.result
                            val flow = manager.launchReviewFlow(context, reviewInfo)
                            flow.addOnCompleteListener {
                                LogModel.d("MoodScreenView", "In-app review flow finished")
                            }
                        } else {
                            LogModel.e("MoodScreenView", "Failed to launch in-app review: ${task.exception}")
                        }
                    }
                }
            },
            onDismissRequest = {
                showAdsSaveMoodDialog = false
            },
        )
    }

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Text(
            text = stringResource(R.string.mood_title),
            style = LocalMainTypography.current.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow {
            items(emotionUiState.emotionList) { emotion ->
                val selected = uiState.selectedMood == emotion
                Text(
                    text = emotion.emoji,
                    fontSize = if (selected) 36.sp else 30.sp,
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            viewModel.selectMood(emotion)
                        }
                        .then(
                            if (selected) Modifier.border(
                                BorderStroke(2.dp, LocalMainColorScheme.current.primary),
                                shape = CircleShape
                            ) else Modifier
                        )
                )
            }

            item {
                IconButton(
                    onClick = {
                        if (!isPremium) {
                            showPremiumUpgradeDialog = true
                        } else {
                            showAddCustomMoodDialog = true
                        }
                    },
                    modifier = Modifier
                        .background(
                            color = LocalPremiumColor.current.background,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = "Add Emoji",
                        modifier = Modifier.size(36.dp),
                        tint = LocalPremiumColor.current.text,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        MoodMemoCard(
            memo = uiState.memo,
            onMemoChange = {
                if (it.length <= 300) viewModel.updateMemo(it)
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        val isPremiumButton = uiState.todaySavedCount > 0

        DefaultButton(
            onClick = {
                val limit = if (isPremium) 3 else 1

                when {
                    uiState.todaySavedCount >= limit -> {
                        if (isPremium) {
                            ToastModel.showToast(context, EnumClass.ToastType.MoodSaveLimit)
                        } else {
                            showPremiumUpgradeDialog = true
                        }
                    }
                    isPremium -> {
                        viewModel.saveMood()

                        val manager = ReviewManagerFactory.create(context)
                        val request = manager.requestReviewFlow()

                        request.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                (context as? Activity)?.also {
                                    // レビューフロー開始
                                    val reviewInfo = task.result
                                    val flow = manager.launchReviewFlow(it, reviewInfo)
                                    flow.addOnCompleteListener {
                                        LogModel.d("MoodScreenView", "In-app review flow finished")
                                    }
                                }
                            } else {
                                LogModel.e("MoodScreenView", "Failed to launch in-app review: ${task.exception}")
                            }
                        }
                    }
                    else -> {
                        showAdsSaveMoodDialog = true
                    }
                }
            },
            enabled = uiState.selectedMood != null && !uiState.isLoading && (isPremium || MobileAdsModel.rewardedAdLoaded),
            buttonColor = if (!isPremiumButton) {
                null
            } else {
                LocalPremiumColor.current.background
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = LocalMainColorScheme.current.onSecondary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.common_save),
                    color = if (!isPremiumButton) {
                        LocalMainColorScheme.current.onSecondary
                    } else {
                        LocalPremiumColor.current.text
                    }
                )
            }
        }

        if (!isPremium && !rewardedAdLoaded) {
            Text(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.End),
                text = stringResource(R.string.mood_save_ads_loading),
                style = LocalMainTypography.current.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.aiComment.isNotEmpty()) {
            Text(
                text = stringResource(R.string.mood_ai_comment_title),
                style = LocalMainTypography.current.titleMedium
            )

            AiCommentCard(
                comment = uiState.aiComment,
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // 下へ押し出す

        OutlinedButton(
            onClick = {
                (context as? ComponentActivity)?.also {
                    MobileAdsModel.showInterstitialAd(it)
                }
                navController.navigate("mood_history")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, LocalMainColorScheme.current.primary)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.mood_history_title))
        }
    }

    if (isLoading) {
        LoadingScreenView()
    }
}

@Composable
fun AddCustomMoodDialog(
    onDismiss: () -> Unit,
    onConfirm: (MoodEmotion.Custom) -> Unit
) {
    var emoji by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.mood_add_custom_mood_title)) },
        text = {
            OutlinedTextField(
                value = emoji,
                onValueChange = { emoji = it.take(2) }, // 絵文字1個基準 (長く続く場合も考慮)
                label = { Text(stringResource(R.string.mood_add_custom_mood_emoji_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), // 絵文字キーボードを誘導
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (emoji.isNotBlank()) {
                        onConfirm(MoodEmotion.Custom(emoji))
                        onDismiss()
                    }
                },
                enabled = emoji.isNotBlank()
            ) {
                Text(
                    text = stringResource(R.string.common_save),
                    color = Blue
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.common_cancel),
                    color = Red
                )
            }
        }
    )
}

@Composable
fun MoodMemoCard(
    memo: String,
    onMemoChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = 300
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = LocalMainColorScheme.current.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.mood_memo_hint),
                style = LocalMainTypography.current.titleSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = memo,
                onValueChange = {
                    if (it.length <= maxLength) onMemoChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 6, // 余裕のある改行を許可
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Default, // エンター → 改行を許可
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                placeholder = {
                    Text(stringResource(R.string.mood_memo_placeholder))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LocalMainColorScheme.current.primary,
                    unfocusedBorderColor = LocalMainColorScheme.current.primary,
                    cursorColor = LocalTextColor.current
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${memo.length} / $maxLength",
                modifier = Modifier
                    .align(Alignment.End),
                style = LocalMainTypography.current.labelSmall.copy(
                    color = LocalMainColorScheme.current.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@Composable
fun AiCommentCard(comment: String, modifier: Modifier = Modifier) {
    if (comment.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = LocalMainColorScheme.current.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Text(
            text = comment.trim(),
            modifier = Modifier.padding(16.dp),
            style = LocalMainTypography.current.bodyMedium,
            color = LocalMainColorScheme.current.onSecondaryContainer,
            lineHeight = 20.sp
        )
    }
}