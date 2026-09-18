package com.gyoheul.calm_routine.view.splash

import android.app.Application
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.data.MobileAdsModel
import com.gyoheul.calm_routine.data.NetworkModel
import com.gyoheul.calm_routine.view.BaseViewModel

class SplashScreenViewModel(
    private val application: Application
) : BaseViewModel(application) {
    override suspend fun initialize() {
        if (NetworkModel.hasInternetAccess()) {
            super.initialize()
            MobileAdsModel.initialize(application)
        } else {
            dialogType = EnumClass.DialogType.ErrorNetwork
            isDisplayDialog.value = true
        }
    }
}