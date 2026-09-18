package com.gyoheul.calm_routine.data

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.gyoheul.calm_routine.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

object MobileAdsModel {
    private val nativeBottomAdUnitId =
        if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            "ca-app-pub-2706768567707804/3510688331"
        }

    private val rewardedAdUnitId =
        if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/5224354917"
        } else {
            "ca-app-pub-2706768567707804/7357260549"
        }

    private val interstitialAdUnitId =
        if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/1033173712"
        } else {
            "ca-app-pub-2706768567707804/1899568666"
        }

    private var rewardedAd: RewardedAd? = null
    var rewardedAdLoaded by mutableStateOf(false)
        private set

    private var interstitialAd: InterstitialAd? = null
    var interstitialAdLoaded by mutableStateOf(false)
        private set

    val isShowAd = !BuildConfig.DEBUG

    fun initialize(context: Context) {
        MobileAds.initialize(context) {
            LogModel.d(this, "MobileAds is initialized.")
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000.milliseconds)
                loadRewardedAd(context)
                loadInterstitialAd(context)
            }
        }
    }

    suspend fun loadNativeBottomAd(context: Context): NativeAd? {
        return suspendCancellableCoroutine { cont ->
            val adLoader = AdLoader
                .Builder(context, nativeBottomAdUnitId)
                .forNativeAd { nativeAd ->
                    cont.resume(nativeAd)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        cont.resume(null)
                    }
                })
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    fun loadRewardedAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            rewardedAdUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    rewardedAdLoaded = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    LogModel.e(this, "loadRewardedAd failed to load: $error")
                    rewardedAdLoaded = false
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, onReward: () -> Unit) {
        if (!isShowAd) {
            onReward()
            return
        }
        rewardedAd?.run {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadRewardedAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    LogModel.e(this, "RewardedAd failed to show.")
                }

                override fun onAdShowedFullScreenContent() {
                    rewardedAd = null
                    rewardedAdLoaded = false
                }
            }
            show(activity) { _ ->
                onReward()
            }
        }
    }

    fun loadInterstitialAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            interstitialAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    interstitialAdLoaded = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    LogModel.e(this, "loadInterstitialAd failed to load: $error")
                    interstitialAdLoaded = false
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity) {
        interstitialAd?.run {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadInterstitialAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    LogModel.e(this, "InterstitialAd failed to show.")
                }

                override fun onAdShowedFullScreenContent() {
                    interstitialAd = null
                    interstitialAdLoaded = false
                }
            }
            show(activity)
        }
    }
}