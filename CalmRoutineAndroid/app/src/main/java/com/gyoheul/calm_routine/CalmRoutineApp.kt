package com.gyoheul.calm_routine

import android.app.Application
import com.gyoheul.calm_routine.data.BillingManager
import com.gyoheul.calm_routine.data.LogModel
import com.gyoheul.calm_routine.data.PremiumStatusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CalmRoutineApp : Application() {
    companion object {
        lateinit var billingManager: BillingManager
            private set
    }

    private fun setBillingManager(billingManager: BillingManager) {
        CalmRoutineApp.billingManager = billingManager
    }

    override fun onCreate() {
        super.onCreate()
        setBillingManager(
            BillingManager(this, object : BillingManager.BillingListener {
                override fun onPremiumPurchased() {
                    LogModel.e(this@CalmRoutineApp, "onPremiumPurchased")
                    PremiumStatusRepository.updatePremiumStatus(true)
                }

                override fun onPurchaseFailed(reason: String) {
                    PremiumStatusRepository.updatePremiumStatus(false)
                    LogModel.e(this@CalmRoutineApp, "onPurchaseFailed reason: $reason")
                }
            }).apply {
                CoroutineScope(Dispatchers.IO).launch {
                    val result: Boolean = startConnection()
                    if (result) {
                        val isPremium: Boolean = checkPremiumStatus()
                        PremiumStatusRepository.updatePremiumStatus(isPremium)
                        LogModel.d(this@CalmRoutineApp, "isPremium: $isPremium")
                    }
                }
            }
        )
    }
}