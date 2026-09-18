package com.gyoheul.calm_routine.view.routine

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gyoheul.calm_routine.R
import com.gyoheul.calm_routine.model.RoutineRecord
import com.gyoheul.calm_routine.ui.theme.Blue
import com.gyoheul.calm_routine.ui.theme.LocalMainColorScheme
import com.gyoheul.calm_routine.ui.theme.LocalMainTypography
import com.gyoheul.calm_routine.ui.theme.PastelSkyBlue
import com.gyoheul.calm_routine.ui.theme.Red
import com.gyoheul.calm_routine.view.LoadingScreenView
import com.gyoheul.calm_routine.view.ShadowBox
import kotlinx.coroutines.launch

@Composable
fun RoutineScreenView(viewModel: RoutineScreenViewModel = viewModel()) {
    val isLoading by viewModel.isLoading

    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val draggedIndex = remember { mutableIntStateOf(-1) }
    val dragOffset = remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current

    val focusManager = LocalFocusManager.current

    val completed by viewModel.completedCount.collectAsState()
    val total by viewModel.totalCount.collectAsState()
    val rate by viewModel.completionRate.collectAsState()

    val deletedMsg = stringResource(R.string.routine_deleted)
    val undoMsg = stringResource(R.string.common_undo)

    var showDialog by remember { mutableStateOf(false) }
    var newRoutineTitle by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = LocalMainColorScheme.current.primary,
                contentColor = LocalMainColorScheme.current.onPrimary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.routine_add)
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = LocalMainColorScheme.current.primary,
                    contentColor = LocalMainColorScheme.current.onPrimary,
                    actionColor = LocalMainColorScheme.current.onPrimary
                )
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        ShadowBox(
            orientation = Orientation.Vertical,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    }
                    .padding(horizontal = 24.dp)
            ) {
                RoutineCompletionBar(
                    completed = completed,
                    total = total,
                    completionRate = rate
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.routine_title),
                    style = LocalMainTypography.current.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    itemsIndexed(uiState.routines, key = { _, item -> item.id }) { index, routine ->
                        val isDragged = draggedIndex.intValue == index
                        val scale by animateFloatAsState(if (isDragged) 1.03f else 1f, label = "")
                        val alpha by animateFloatAsState(if (isDragged) 0.85f else 1f, label = "")

                        RoutineCheckboxItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(3.dp)
                                .graphicsLayer {
                                    this.scaleX = scale
                                    this.scaleY = scale
                                    this.alpha = alpha
                                }
                                .animateItem(
                                    fadeInSpec = tween(durationMillis = 250),
                                    fadeOutSpec = tween(durationMillis = 100),
                                    placementSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
                                )
                                .background(
                                    if (isDragged) PastelSkyBlue
                                    else LocalMainColorScheme.current.surface,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggedIndex.intValue = index
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDragEnd = {
                                            draggedIndex.intValue = -1
                                            dragOffset.floatValue = 0f
                                        },
                                        onDragCancel = {
                                            draggedIndex.intValue = -1
                                            dragOffset.floatValue = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset.floatValue += dragAmount.y

                                            val current = draggedIndex.intValue
                                            val newIndex = (current + (dragOffset.floatValue / 100).toInt())
                                                .coerceIn(0, uiState.routines.lastIndex)

                                            if (newIndex != current) {
                                                scope.launch {
                                                    viewModel.moveRoutine(current, newIndex)
                                                    draggedIndex.intValue = newIndex
                                                    dragOffset.floatValue = 0f
                                                }
                                            }
                                        }
                                    )
                                },
                            routine = routine,
                            onCheckedChange = {
                                viewModel.toggleRoutine(routine.id, it)
                            },
                            onTitleChange = {
                                viewModel.updateRoutineTitle(routine.id, it)
                            },
                            onDelete = {
                                viewModel.deleteRoutine(routine.id)

                                scope.launch {
                                    val result = snackBarHostState.showSnackbar(
                                        message = deletedMsg,
                                        actionLabel = undoMsg
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restoreRoutine()
                                    }
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }
        }

        // ルーチン追加ダイアログ
        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    newRoutineTitle = ""
                },
                title = {
                    Text(text = stringResource(R.string.routine_add_title))
                },
                text = {
                    TextField(
                        value = newRoutineTitle,
                        onValueChange = { newRoutineTitle = it },
                        placeholder = {
                            Text(stringResource(R.string.routine_add_hint))
                        },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newRoutineTitle.isNotBlank()) {
                                viewModel.addRoutine(newRoutineTitle.trim())
                            }
                            newRoutineTitle = ""
                            showDialog = false
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.common_add),
                            color = Blue
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            newRoutineTitle = ""
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.common_cancel),
                            color = Red
                        )
                    }
                }
            )
        }
    }

    if (isLoading) {
        LoadingScreenView()
    }
}

@Composable
fun RoutineCompletionBar(
    completed: Int,
    total: Int,
    completionRate: Float
) {
    if (total == 0) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = R.string.routine_empty_hint),
                modifier = Modifier.padding(16.dp)
            )
        }
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(
                    id = R.string.routine_completion_text,
                    completed,
                    total,
                    (completionRate * 100).toInt()
                ),
                style = LocalMainTypography.current.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { completionRate },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = LocalMainColorScheme.current.primary
            )
        }
    }
}

@Composable
fun RoutineCheckboxItem(
    modifier: Modifier,
    routine: RoutineRecord,
    onCheckedChange: (Boolean) -> Unit,
    onTitleChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf(routine.title) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Checkbox(
            checked = routine.isDone,
            onCheckedChange = onCheckedChange
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (isEditing) {
            TextField(
                value = title,
                onValueChange = { title = it },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = LocalMainTypography.current.bodyLarge,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        isEditing = false
                        onTitleChange(title)
                    }
                ),
                placeholder = {
                    Text(
                        text = stringResource(R.string.routine_edit_hint)
                    )
                },
            )
        } else {
            Text(
                text = routine.title,
                modifier = Modifier
                    .weight(1f)
                    .clickable { isEditing = true },
                style = LocalMainTypography.current.bodyLarge
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete"
            )
        }
    }
}
