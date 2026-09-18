package com.gyoheul.calm_routine.view

import android.app.Activity.RESULT_OK
import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseUser
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.data.FirebaseModel
import com.gyoheul.calm_routine.data.NetworkModel
import com.gyoheul.calm_routine.data.PremiumStatusRepository
import com.gyoheul.calm_routine.data.SQLiteManager
import com.gyoheul.calm_routine.data.SharedPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class BaseViewModel(
    private val application: Application
) : AndroidViewModel(application) {
    val isLoading = mutableStateOf(false)
    val isLoggedIn = mutableStateOf(false)
    val isDisplayDialog = mutableStateOf(false)
    var dialogType = EnumClass.DialogType.None
    val user = mutableStateOf<FirebaseUser?>(null)

    val preferencesManager = SharedPreferencesManager(application)
    val sqLiteManager = SQLiteManager(application)
    val isPremium = PremiumStatusRepository.isPremium

    open suspend fun initialize() {
        fetchUserData()
    }

    fun onDialogDismiss() {
        isDisplayDialog.value = false
        dialogType = EnumClass.DialogType.None
    }

    private suspend fun fetchUserData() {
        if (NetworkModel.hasInternetAccess()) {
            user.value = FirebaseModel.getUser()
            isLoggedIn.value = FirebaseModel.getUser() != null
        } else {
            dialogType = EnumClass.DialogType.ErrorNetwork
            isDisplayDialog.value = true
        }
    }

    open fun onLoginClick(
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            if (NetworkModel.hasInternetAccess()) {
                login(onSuccess, onFailure)
            } else {
                dialogType = EnumClass.DialogType.ErrorNetwork
                isDisplayDialog.value = true
            }
        }
    }

    fun login(
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        isLoggedIn.value = false
        isLoading.value = true
        FirebaseModel.signInLauncher { resultCode, user ->
            if (resultCode == RESULT_OK && user != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    fetchUserData()
                    CoroutineScope(Dispatchers.Main).launch {
                        onSuccess()
                        isLoading.value = false
                    }
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    onFailure()
                    isLoading.value = false
                }
            }
        }
    }

    open suspend fun logout(): Boolean {
        return FirebaseModel.signOut(application).also {
            fetchUserData()
        }
    }

    fun showLoading() {
        isLoading.value = true
    }

    fun dismissLoading() {
        isLoading.value = false
    }
}