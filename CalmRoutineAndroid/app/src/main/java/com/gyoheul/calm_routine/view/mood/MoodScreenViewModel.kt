package com.gyoheul.calm_routine.view.mood

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.gyoheul.calm_routine.R
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.common.Extension.getLogicalToday
import com.gyoheul.calm_routine.common.Extension.toLocalDateTime
import com.gyoheul.calm_routine.data.AICommentModel
import com.gyoheul.calm_routine.data.MobileAdsModel
import com.gyoheul.calm_routine.data.OpenAIManager
import com.gyoheul.calm_routine.data.ToastModel
import com.gyoheul.calm_routine.model.EmotionUiState
import com.gyoheul.calm_routine.model.MoodEmotion
import com.gyoheul.calm_routine.model.MoodRecord
import com.gyoheul.calm_routine.model.MoodUiState
import com.gyoheul.calm_routine.model.ProfileData
import com.gyoheul.calm_routine.view.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.Date
import kotlin.reflect.KProperty0

class MoodScreenViewModel(
    private val application: Application
) : BaseViewModel(application) {
    private val openAIManager = OpenAIManager()

    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState: StateFlow<MoodUiState> = _uiState.asStateFlow()

    private val _emotionUiState = MutableStateFlow(EmotionUiState())
    val emotionUiState: StateFlow<EmotionUiState> = _emotionUiState.asStateFlow()

    val rewardedAdLoaded: KProperty0<Boolean> = MobileAdsModel::rewardedAdLoaded

    override suspend fun initialize() {
        isLoading.value = true

        super.initialize()
        if (!rewardedAdLoaded.get()) {
            MobileAdsModel.loadRewardedAd(application)
        }
        preferencesManager.removeOldMoodTodayCountKeys()
        loadMoodInfo()
        viewModelScope.launch {
            isLoading.value = false
        }
    }

    private fun loadMoodInfo() {
        viewModelScope.launch {
            val records = sqLiteManager.getMoods(1)
            val todayMood = records.firstOrNull {
                it.date == LocalDateTime.now().getLogicalToday().toString()
            }

            _uiState.update { state ->
                state.copy(
                    selectedMood = todayMood?.emotion,
                    memo = todayMood?.memo ?: "",
                    isLoading = false,
                    aiComment = todayMood?.aiComment ?: "",
                    todaySavedCount = preferencesManager.getMoodToadySavedCount()
                )
            }

            val predefinedList = EnumClass.EmotionType.entries.map {
                MoodEmotion.Predefined(it)
            }
            val customList = preferencesManager.getCustomMoods().toList()
            val fullList: List<MoodEmotion> = customList + predefinedList
            val recentList = preferencesManager.getRecentMoods()
            val recentPart = recentList.mapNotNull { mood ->
                fullList.find { it == mood }
            }
            val remainingPart = fullList.filterNot { it in recentList }
            _emotionUiState.update { state ->
                state.copy(
                    emotionList = recentPart + remainingPart
                )
            }
        }
    }

    fun addCustomMood(mood: MoodEmotion.Custom) {
        if (EnumClass.EmotionType.entries.any { it.emoji == mood.emoji }) {
            return
        }
        preferencesManager.addCustomMood(mood)
        _emotionUiState.update { state ->
            state.copy(
                emotionList = listOf(mood) + state.emotionList
            )
        }
    }

    fun selectMood(mood: MoodEmotion) {
        _uiState.update { state ->
            state.copy(selectedMood = mood)
        }
    }

    fun updateMemo(text: String) {
        if (text.length <= 300) {
            _uiState.update { state ->
                state.copy(memo = text)
            }
        }
    }

    fun saveMood() {
        val mood = _uiState.value.selectedMood ?: return
        val memo = _uiState.value.memo

        _uiState.update { state ->
            state.copy(isLoading = true)
        }

        viewModelScope.launch {
            try {
                val moods = sqLiteManager.getMoods(limit = 5, desc = false)
                val aiCommentStyle = preferencesManager.getAICommentStyle(EnumClass.AICommentStyle.Friendly)
                val userMessage = AICommentModel.getUserMoodMessage(
                    mood = mood,
                    memo = memo,
                    style = aiCommentStyle
                )
                val profileData: ProfileData? = preferencesManager.getSettingsProfileData()
                val response = openAIManager.sendMessageToChatGPT(
                    buildList {
                        add(AICommentModel.getMoodSystemPrompt(aiCommentStyle, profileData))
                        addAll(AICommentModel.getHistoryMessagesByMoods(moods))
                        add(userMessage)
                    }
                ) // ← GPT API リクエスト
                if (response?.content?.isNotBlank() == true) {
                    _uiState.update {
                        it.copy(
                            aiComment = response.content,
                            isLoading = false
                        )
                    }
                    withContext(Dispatchers.Main) {
                        saveMoodRecord(userMessage.content)
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            aiComment = application.getString(R.string.mood_comment_error),
                            isLoading = false
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        aiComment = application.getString(R.string.mood_comment_error),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun saveMoodRecord(prompt: String) {
        ToastModel.showToast(
            application,
            _uiState.value.selectedMood?.let { selectedMood ->
                val record = MoodRecord(
                    date = getTodayDate(),
                    emotion = selectedMood,
                    memo = _uiState.value.memo,
                    prompt = prompt,
                    aiComment = _uiState.value.aiComment
                )
                sqLiteManager.upsertMood(record)
                preferencesManager.setRecentMood(selectedMood)
                preferencesManager.addMoodToadySavedCount()

                _uiState.update {
                    it.copy(
                        todaySavedCount = preferencesManager.getMoodToadySavedCount()
                    )
                }
                EnumClass.ToastType.SuccessSaveMood
            } ?: EnumClass.ToastType.FailSaveMood
        )
    }

    private fun getTodayDate(): String {
        return Date().toLocalDateTime().getLogicalToday().toString()
    }
}