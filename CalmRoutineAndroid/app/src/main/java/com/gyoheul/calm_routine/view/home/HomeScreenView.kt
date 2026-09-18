package com.gyoheul.calm_routine.view.home

import androidx.activity.ComponentActivity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.gyoheul.calm_routine.R
import com.gyoheul.calm_routine.data.MobileAdsModel
import com.gyoheul.calm_routine.model.MoodRecord
import com.gyoheul.calm_routine.ui.theme.ComponentStyles
import com.gyoheul.calm_routine.ui.theme.LocalMainColorScheme
import com.gyoheul.calm_routine.ui.theme.LocalMainTypography
import com.gyoheul.calm_routine.view.DefaultTextButton
import com.gyoheul.calm_routine.view.LoadingScreenView
import com.gyoheul.calm_routine.view.moodHistory.MoodHistoryCard

@Composable
fun HomeScreenView(mainNavController: NavHostController, navController: NavHostController, viewModel: HomeScreenViewModel = viewModel()) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    Column {
        Text(
            text = stringResource(R.string.home_title),
            style = LocalMainTypography.current.titleLarge,
            modifier = Modifier
                .padding(
                    vertical = 16.dp,
                    horizontal = 24.dp
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            item {
                TodayRoutineView()
            }

            item {
                TodayMoodView(navController)
            }

            item {
                RecentMoodsView(
                    records = uiState.recentMoodRecords,
                    onViewAllClick = {
                        (context as? ComponentActivity)?.also {
                            MobileAdsModel.showInterstitialAd(it)
                        }
                        mainNavController.navigate("mood_history")
                    }
                )
            }
        }
    }

    if (isLoading) {
        LoadingScreenView()
    }
}

@Composable
fun TodayRoutineView(
    viewModel: HomeScreenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val routines = uiState.todayRoutines
    val incompleteRoutineTitles = routines.filter { !it.isDone }.map { it.title }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.home_today_routine_title),
            style = LocalMainTypography.current.titleMedium
        )

        // ルーチン完了の要約を表示
        Text(
            text = stringResource(
                R.string.home_today_routine_summary,
                routines.count { it.isDone },
                routines.size
            ),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = LocalMainTypography.current.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (incompleteRoutineTitles.isNotEmpty()) {
            Text(
                text = stringResource(R.string.home_today_routine_incomplete),
                style = LocalMainTypography.current.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            incompleteRoutineTitles.forEach {
                RoutineItem(title = it)
            }
        }
    }
}

@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun RoutineItem(title: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .styleable(style = ComponentStyles.routineCard(LocalMainColorScheme.current.surface)),
        tonalElevation = 2.dp
    ) {
        Text(
            text = "• $title",
            modifier = Modifier.padding(12.dp),
            style = LocalMainTypography.current.bodyMedium
        )
    }
}

@Composable
fun TodayMoodView(
    navController: NavHostController,
    viewModel: HomeScreenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // 感情記録の状態を確認
        if (uiState.todayMood != null) {
            val moodRecord = uiState.todayMood!!
            Text(
                text = stringResource(R.string.home_today_mood_title),
                style = LocalMainTypography.current.titleMedium
            )

            Text(
                text = "${moodRecord.emotion.emoji}  ${moodRecord.memo}",
                style = LocalMainTypography.current.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = LocalMainColorScheme.current.secondary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💬 ${moodRecord.aiComment}",
                    style = LocalMainTypography.current.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Text(
                text = stringResource(R.string.home_today_mood_prompt),
                style = LocalMainTypography.current.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            DefaultTextButton(
                title = stringResource(R.string.home_today_mood_record),
                onClick = {
                    navController.navigate("mood") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
fun RecentMoodsView(
    records: List<MoodRecord>,
    onViewAllClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.home_recent_moods_title),
                style = LocalMainTypography.current.titleMedium
            )
            TextButton(onClick = onViewAllClick) {
                Text(text = stringResource(id = R.string.home_recent_moods_view_all))
            }
        }

        if (records.isEmpty()) {
            Text(
                text = stringResource(id = R.string.home_recent_moods_not_yet),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = LocalMainTypography.current.bodyMedium
            )
        } else {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .height(IntrinsicSize.Max)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                records.map { record ->
                    MoodHistoryCard(
                        record = record,
                        modifier = Modifier
                            .width(160.dp)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}
