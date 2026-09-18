package com.gyoheul.calm_routine.data

import android.content.Context
import androidx.activity.ComponentActivity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class BillingManager(
    context: Context,
    private val billingListener: BillingListener
) {
    private val purchasesUpdatedListener: PurchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    // Process the purchase as described in the next section.
                    LogModel.d(this, "onPurchasesUpdated: ${purchase.purchaseState}")

                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()

                        billingClient.acknowledgePurchase(acknowledgeParams) { ackResult ->
                            if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                LogModel.d(this, "Purchase acknowledged")
                                // 有料機能の解放などの処理
                                billingListener.onPremiumPurchased()
                            } else {
                                billingListener.onPurchaseFailed("Acknowledge failed: ${ackResult.responseCode}")
                            }
                        }
                    }
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                billingListener.onPurchaseFailed("User canceled")
            } else {
                billingListener.onPurchaseFailed("Error: ${billingResult.debugMessage}")
            }
        }

    private var billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    suspend fun startConnection(retry: Int = 0): Boolean {
        if (billingClient.isReady) {
            return true
        }
        if (retry > 3) {
            return false
        }
        return suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                        cont.resume(true)
                    } else {
                        LogModel.e(this@BillingManager, "startConnection: ${billingResult.responseCode}")
                        cont.resume(false)
                    }
                }
                override fun onBillingServiceDisconnected() {
                    CoroutineScope(Dispatchers.IO).launch {
                        cont.resume(startConnection(retry + 1))
                    }
                }
            })
        }
    }

    suspend fun checkPremiumStatus(): Boolean {
        val result = suspendCancellableCoroutine { cont ->
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { billingResult, purchasesList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val hasActive = purchasesList.any { purchase ->
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED && purchase.isAcknowledged
                    }
                    cont.resume(hasActive)
                } else {
                    cont.resume(false)
                }
            }
        }
        return result
    }

    suspend fun getProductDetails(): ProductDetails? {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("premium")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
        params.setProductList(productList)

        // leverage queryProductDetails Kotlin extension function
        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient.queryProductDetails(params.build())
        }

        // Process the result.
        return if (productDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            LogModel.d(this, "getProductDetails: ${productDetailsResult.productDetailsList?.firstOrNull()}")
            productDetailsResult.productDetailsList?.firstOrNull()
        } else {
            LogModel.e(this, "getProductDetails: ${productDetailsResult.billingResult.responseCode} message: ${productDetailsResult.billingResult.debugMessage}")
            null
        }
    }

    fun launchBillingFlow(activity: ComponentActivity, productDetails: ProductDetails?, offerIdToken: String) {
        if (productDetails == null) return

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(productDetails)
                // Get the offer token:
                // a. For one-time products, call ProductDetails.getOneTimePurchaseOfferDetailsList()
                // for a list of offers that are available to the user.
                // b. For subscriptions, call ProductDetails.subscriptionOfferDetails()
                // for a list of offers that are available to the user.
                .setOfferToken(offerIdToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .setIsOfferPersonalized(true)
            .build()

        // Launch the billing flow
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    interface BillingListener {
        fun onPremiumPurchased()
        fun onPurchaseFailed(reason: String)
    }
}