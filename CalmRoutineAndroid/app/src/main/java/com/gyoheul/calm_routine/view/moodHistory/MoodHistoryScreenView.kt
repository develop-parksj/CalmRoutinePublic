package com.gyoheul.calm_routine.view.moodHistory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.gyoheul.calm_routine.R
import com.gyoheul.calm_routine.model.MoodRecord
import com.gyoheul.calm_routine.ui.theme.ComponentStyles
import com.gyoheul.calm_routine.ui.theme.LocalMainColorScheme
import com.gyoheul.calm_routine.ui.theme.LocalMainTypography
import com.gyoheul.calm_routine.view.DefaultTopAppBar
import com.gyoheul.calm_routine.view.LoadingScreenView
import com.gyoheul.calm_routine.view.NativeBottomAdView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import androidx.compose.ui.platform.LocalLocale

@Composable
fun MoodHistoryScreenView(navController: NavHostController, viewModel: MoodHistoryScreenViewModel = viewModel()) {
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
                title = stringResource(R.string.mood_history_title),
                onNavigationClick = {
                    navController.popBackStack()
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.onShowTypeChangeClick()
                        }
                    ) {
                        Icon(
                            if (uiState.isShowCalendar) {
                                Icons.AutoMirrored.Filled.List
                            } else {
                                Icons.Default.CalendarMonth
                            },
                            contentDescription = if (uiState.isShowCalendar) {
                                "List"
                            } else {
                                "CalendarMonth"
                            },
                            tint = LocalMainColorScheme.current.onPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.mood_history_no_records)
                    )
                }
            } else if (uiState.isShowCalendar) {
                MoodHistoryCalendarView(
                    onDateSelected = { mood ->
                        viewModel.onMoodClick(mood)
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                if (uiState.selectedMood != null) {
                    MoodHistoryCard(
                        record = uiState.selectedMood!!,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                }
            } else {
                MoodHistoryListView(
                    modifier = Modifier
                        .weight(1f)
                )
            }

            NativeBottomAdView(nativeBottomAd)
        }
    }

    if (isLoading) {
        LoadingScreenView()
    }
}

@Composable
fun MoodHistoryCalendarView(
    viewModel: MoodHistoryScreenViewModel = viewModel(),
    onDateSelected: (MoodRecord?) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val recordMap = uiState.records.associateBy {
        LocalDate.parse(it.date)
    }

    val currentMonth = remember { mutableStateOf(YearMonth.now()) }
    val selectedDate = remember { mutableStateOf(LocalDate.now()) }

    val firstDayOfMonth = currentMonth.value.atDay(1)
    val lastDayOfMonth = currentMonth.value.atEndOfMonth()
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
    val daysInMonth = currentMonth.value.lengthOfMonth()

    // 先月の最終週の一部
    val prevMonth = currentMonth.value.minusMonths(1)
    val prevMonthEnd = prevMonth.atEndOfMonth()
    val leadingDays = List(startDayOfWeek) { index ->
        prevMonthEnd.minusDays((startDayOfWeek - 1 - index).toLong())
    }

    // 今月
    val thisMonthDays = (1..daysInMonth).map { currentMonth.value.atDay(it) }

    // 来月の冒頭部分
    val totalCells = leadingDays.size + thisMonthDays.size
    val trailingDaysCount = (7 - (totalCells % 7)).let { if (it == 7) 0 else it }
    val nextMonth = currentMonth.value.plusMonths(1)
    val trailingDays = List(trailingDaysCount) { index ->
        nextMonth.atDay(index + 1)
    }

    val calendarDays = leadingDays + thisMonthDays + trailingDays

    Column {
        // 月のヘッダー
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = {
                    currentMonth.value = currentMonth.value.minusMonths(1)
                }
            ) {
                Text("<")
            }
            Text(
                text = currentMonth.value.month.getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale) +
                        " " +
                        currentMonth.value.year,
                style = LocalMainTypography.current.titleMedium
            )
            TextButton(
                onClick = {
                    currentMonth.value = currentMonth.value.plusMonths(1)
                }
            ) {
                Text(">")
            }
        }

        Spacer(Modifier.height(8.dp))

        // 曜日の表示
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach {
                Text(
                    text = it,
                    style = LocalMainTypography.current.labelLarge,
                    modifier = Modifier
                        .weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        HorizontalDivider()

        Spacer(Modifier.height(4.dp))

        // 日付グリッド
        calendarDays.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                week.forEach { date ->
                    val isSelected = date == selectedDate.value
                    val isCurrentMonth = date.month == currentMonth.value.month
                    val backgroundColor =
                        if (isSelected) {
                            LocalMainColorScheme.current.primary
                        } else {
                            Color.Transparent
                        }
                    val textColor = when {
                        !isCurrentMonth -> LocalMainColorScheme.current.outline
                        isSelected -> LocalMainColorScheme.current.onPrimary
                        else -> LocalMainColorScheme.current.onSurface
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(backgroundColor)
                            .clickable {
                                when {
                                    date.isBefore(firstDayOfMonth) -> {
                                        currentMonth.value = currentMonth.value.minusMonths(1)
                                    }
                                    date.isAfter(lastDayOfMonth) -> {
                                        currentMonth.value = currentMonth.value.plusMonths(1)
                                    }
                                }
                                selectedDate.value = date
                                onDateSelected(recordMap[date])
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            fontSize = 12.sp,
                            color = textColor,
                            style = LocalMainTypography.current.labelSmall
                        )
                        if (recordMap[date] != null) {
                            Text(
                                text = recordMap[date]!!.emotion.emoji,
                                style = LocalMainTypography.current.bodyMedium
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
fun MoodHistoryListView(
    modifier: Modifier = Modifier,
    viewModel: MoodHistoryScreenViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        items(uiState.records) { record ->
            MoodHistoryCard(
                record = record
            )
        }
    }
}

@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun MoodHistoryCard(
    record: MoodRecord,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .styleable(style = ComponentStyles.moodCard(LocalMainColorScheme.current.surface)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 日付
            Text(
                text = record.date,
                style = LocalMainTypography.current.labelSmall,
                color = LocalMainColorScheme.current.outline
            )

            Spacer(Modifier.height(8.dp))

            // 感情絵文字 + 名前
            Text(text = record.emotion.emoji, style = LocalMainTypography.current.titleLarge)

            // メモ
            if (record.memo.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.mood_history_memo_label),
                    style = LocalMainTypography.current.labelMedium
                )
                Text(
                    text = record.memo,
                    style = LocalMainTypography.current.bodyMedium
                )
            }

            // AIコメント
            if (record.aiComment.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.mood_history_ai_comment_label),
                    style = LocalMainTypography.current.labelMedium
                )
                Text(
                    text = record.aiComment,
                    style = LocalMainTypography.current.bodySmall,
                    color = LocalMainColorScheme.current.onPrimary
                )
            }
        }
    }
}