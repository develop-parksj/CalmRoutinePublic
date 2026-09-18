package com.gyoheul.calm_routine.view.routine

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.gyoheul.calm_routine.model.RoutineRecord
import com.gyoheul.calm_routine.model.RoutineUiState
import com.gyoheul.calm_routine.view.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutineScreenViewModel(
    application: Application
) : BaseViewModel(application) {
    private val _uiState = MutableStateFlow(RoutineUiState())
    val uiState: StateFlow<RoutineUiState> = _uiState

    private var recentlyDeletedRoutine: RoutineRecord? = null

    val completedCount: StateFlow<Int> = uiState
        .map { state -> state.routines.count { it.isDone } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val totalCount: StateFlow<Int> = uiState
        .map { state -> state.routines.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val completionRate: StateFlow<Float> = combine(completedCount, totalCount) { completed, total ->
        if (total == 0) 0f else completed.toFloat() / total
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    override suspend fun initialize() {
        isLoading.value = true

        super.initialize()

        loadRoutines()

        viewModelScope.launch {
            isLoading.value = false
        }
    }

    private fun loadRoutines() {
        _uiState.update { state ->
            state.copy(
                routines = sqLiteManager.getTodayRoutines()
            )
        }
    }

    fun toggleRoutine(routineId: String, isChecked: Boolean) {
        _uiState.update { state ->
            val updated = state.routines.map {
                if (it.id == routineId) it.copy(isDone = isChecked) else it
            }
            sqLiteManager.upsertRoutine(
                updated.first { it.id == routineId }
            )
            state.copy(routines = updated)
        }
    }

    fun addRoutine(title: String) {
        if (title.isBlank()) return // 空文字列を無視
        _uiState.update { state ->
            val newRoutine = RoutineRecord(
                title = title.trim(),
                isDone = false,
                sortIndex = state.routines.maxOfOrNull { it.sortIndex + 1 } ?: 0
            )
            val updated = state.routines + newRoutine
            sqLiteManager.upsertRoutine(newRoutine)
            state.copy(routines = updated)
        }
    }

    fun updateRoutineTitle(routineId: String, newTitle: String) {
        _uiState.update { state ->
            val updated = state.routines.map {
                if (it.id == routineId) it.copy(title = newTitle) else it
            }
            sqLiteManager.upsertRoutine(
                updated.first { it.id == routineId }
            )
            state.copy(routines = updated)
        }
    }

    fun deleteRoutine(routineId: String) {
        val deleted = _uiState.value.routines.find { it.id == routineId }
        recentlyDeletedRoutine = deleted // for undo
        _uiState.update { state ->
            val updated = state.routines.filter { it.id != routineId }
            sqLiteManager.deleteRoutine(deleted)
            state.copy(routines = updated)
        }
    }

    fun restoreRoutine() {
        recentlyDeletedRoutine?.also { routine ->
            _uiState.update { state ->
                val undo = (state.routines + routine).sortedBy { it.sortIndex }
                sqLiteManager.upsertRoutine(routine)
                state.copy(routines = undo)
            }
            recentlyDeletedRoutine = null
        }
    }

    fun moveRoutine(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val mutableList = state.routines.toMutableList()
            val item = mutableList.removeAt(fromIndex)
            mutableList.add(toIndex, item)
            mutableList.forEachIndexed { index, routine ->
                routine.sortIndex = index
            }
            viewModelScope.launch {
                sqLiteManager.upsertRoutines(mutableList)
            }
            state.copy(routines = mutableList)
        }
    }
}