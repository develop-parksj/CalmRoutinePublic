package com.gyoheul.calm_routine.view.settings.editProfile

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.nativead.NativeAd
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.data.MobileAdsModel
import com.gyoheul.calm_routine.model.EditProfileUiState
import com.gyoheul.calm_routine.model.ProfileData
import com.gyoheul.calm_routine.view.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class EditProfileScreenViewModel(
    private val application: Application
) : BaseViewModel(application) {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState

    private val _nativeBottomAd = MutableStateFlow<NativeAd?>(null)
    val nativeBottomAd: StateFlow<NativeAd?> = _nativeBottomAd

    override suspend fun initialize() {
        isLoading.value = true

        super.initialize()
        loadUserInfo()
        viewModelScope.launch {
            isLoading.value = false

            _nativeBottomAd.value = MobileAdsModel.loadNativeBottomAd(application)
        }
    }

    private fun loadUserInfo() {
        val profileData: ProfileData = preferencesManager.getSettingsProfileData() ?: ProfileData(
            nickname = user.value?.displayName ?: ""
        )
        preferencesManager.setSettingsProfileData(profileData)
        _uiState.update {
            it.copy(
                email = user.value?.email ?: "",
                nickname = profileData.nickname,
                gender = profileData.gender,
                birthDate = profileData.birthDate,
                interests = profileData.interests,
                isSaved = true
            )
        }
    }

    fun onNicknameChange(newValue: String) {
        _uiState.update { state ->
            state.copy(
                nickname = newValue,
                isSaved = false
            )
        }
    }

    fun onGenderChange(gender: EnumClass.Gender) {
        _uiState.update { state ->
            state.copy(
                gender = gender,
                isSaved = false
            )
        }
    }

    fun onBirthDateChange(date: LocalDate) {
        _uiState.update { state ->
            state.copy(
                birthDate = date,
                isSaved = false
            )
        }
    }

    fun onInterestInputChange(value: String) {
        _uiState.update { state ->
            state.copy(interestInput = value)
        }
    }

    fun onAddInterest() {
        _uiState.update { state ->
            state.copy(
                interests = state.interests + state.interestInput,
                isSaved = false
            )
        }
    }

    fun onRemoveInterest(value: String) {
        _uiState.update { state ->
            state.copy(
                interests = state.interests - value,
                isSaved = false
            )
        }
    }

    fun onSaveClick() {
        _uiState.update { state ->
            state.copy(isSaving = true)
        }
        preferencesManager.setSettingsProfileData(
            ProfileData(
                nickname = uiState.value.nickname,
                gender = uiState.value.gender,
                birthDate = uiState.value.birthDate,
                interests = uiState.value.interests
            )
        )
        _uiState.update { state ->
            state.copy(isSaving = false, isSaved = true)
        }
    }

    fun onReturnClick(): Boolean {
        return _uiState.value.isSaved
    }
}
