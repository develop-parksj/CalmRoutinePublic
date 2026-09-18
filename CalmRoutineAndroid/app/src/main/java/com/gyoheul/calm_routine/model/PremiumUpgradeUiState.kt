package com.gyoheul.calm_routine.model

import androidx.annotation.StringRes
import com.android.billingclient.api.ProductDetails

data class PremiumUpgradeUiState(
    val productDetails: ProductDetails? = null,
    val cards: List<PremiumUpgradeCardUiState> = emptyList(),
    val isEnableData: Boolean = false,
)

data class PremiumUpgradeCardUiState(
    @StringRes val titleId: Int = -1,
    val price: String = "",
    val offerIdToken: String = "",
)