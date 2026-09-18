package com.gyoheul.calm_routine.view.main

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.gyoheul.calm_routine.data.FirebaseModel
import com.gyoheul.calm_routine.view.BaseViewModel
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty0

class MainScreenViewModel(
    application: Application
) : BaseViewModel(application) {
    val firebaseConfigActivated: KProperty0<Boolean> = FirebaseModel::firebaseConfigActivated

    override suspend fun initialize() {
        isLoading.value = true

        super.initialize()
        viewModelScope.launch {
            isLoading.value = false
        }
    }
}