package com.gyoheul.calm_routine.view.settings.premiumUpgrade

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.gyoheul.calm_routine.CalmRoutineApp
import com.gyoheul.calm_routine.common.EnumClass
import com.gyoheul.calm_routine.data.BillingManager
import com.gyoheul.calm_routine.model.PremiumUpgradeCardUiState
import com.gyoheul.calm_routine.model.PremiumUpgradeUiState
import com.gyoheul.calm_routine.view.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Currency

class PremiumUpgradeScreenViewModel(
    application: Application
) : BaseViewModel(application) {
    val billingManager: BillingManager = CalmRoutineApp.billingManager

    private val _uiState = MutableStateFlow(PremiumUpgradeUiState())
    val uiState: StateFlow<PremiumUpgradeUiState> = _uiState

    override suspend fun initialize() {
        isLoading.value = true

        super.initialize()
        viewModelScope.launch {
            loadBillingInfo()
            isLoading.value = false
        }
    }

    private suspend fun loadBillingInfo() {
        val result = billingManager.startConnection()
        if (result) {
            val productDetails: ProductDetails? = billingManager.getProductDetails()
            var monthlyMicros = 0L
            _uiState.update { state ->
                state.copy(
                    productDetails = productDetails,
                    cards = productDetails?.subscriptionOfferDetails?.map { details ->
                        val premiumType: EnumClass.PremiumType =
                            when (details.basePlanId) {
                                "monthly" -> EnumClass.PremiumType.Monthly
                                "yearly" -> EnumClass.PremiumType.Yearly
                                else -> return@map null
                            }
                        val pricingPhase = details.pricingPhases.pricingPhaseList.first()
                        if (premiumType == EnumClass.PremiumType.Monthly) {
                            monthlyMicros = pricingPhase.priceAmountMicros
                        }
                        PremiumUpgradeCardUiState(
                            titleId = premiumType.labelRes,
                            price = when (premiumType) {
                                EnumClass.PremiumType.Monthly -> getNumberFormat(pricingPhase.priceCurrencyCode)
                                    .format(pricingPhase.priceAmountMicros / 1_000_000.0)
                                EnumClass.PremiumType.Yearly -> formatAnnualComparison(
                                    monthlyMicros,
                                    pricingPhase.priceAmountMicros,
                                    pricingPhase.priceCurrencyCode
                                )
                            },
                            offerIdToken = details.offerToken
                        )
                    }?.filterNotNull() ?: emptyList()
                )
            }
        }
    }

    private fun formatAnnualComparison(
        monthlyMicros: Long,
        yearlyMicros: Long,
        currencyCode: String
    ): String {
        val monthly = monthlyMicros / 1_000_000.0
        val yearly = yearlyMicros / 1_000_000.0
        val annualFromMonthly = monthly * 12
        val formatter = getNumberFormat(currencyCode)

        return "${formatter.format(annualFromMonthly)}\n → ${formatter.format(yearly)}"
    }

    private fun getNumberFormat(currencyCode: String) : NumberFormat =
        NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(currencyCode)
        }
}
