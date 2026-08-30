package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.Cyan400
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.Slate400
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

object AdManager {
    private const val TAG = "MindrixAdManager"

    // User's AdMob Ad Unit IDs
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-2330525119428447/8776498040"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-2330525119428447/8536824897"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-2330525119428447/7271844687"

    private var isInitialized = false

    // App Open Ad State
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAppOpenAd = false
    private var appOpenLoadTime: Long = 0
    private var isShowingAppOpenAd = false

    // Rewarded Ad State
    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewardedAd = false
    private val _isRewardedReady = MutableStateFlow(false)
    val isRewardedReady = _isRewardedReady.asStateFlow()

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob MobileAds initialized: $status")
                isInitialized = true
                loadAppOpenAd(context)
                loadRewardedAd(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds", e)
        }
    }

    // ==========================================
    // 1. APP OPEN ADS
    // ==========================================
    fun loadAppOpenAd(context: Context) {
        if (isLoadingAppOpenAd || isAppOpenAdAvailable()) return
        isLoadingAppOpenAd = true

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context.applicationContext,
            APP_OPEN_AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App Open Ad loaded successfully")
                    appOpenAd = ad
                    isLoadingAppOpenAd = false
                    appOpenLoadTime = Date().time
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "App Open Ad failed to load: ${loadAdError.message}")
                    isLoadingAppOpenAd = false
                    appOpenAd = null
                }
            }
        )
    }

    private fun isAppOpenAdAvailable(): Boolean {
        val numHours = 4
        val dateDifference = Date().time - appOpenLoadTime
        val numMilliSecondsPerHour: Long = 3600000
        return appOpenAd != null && (dateDifference < (numMilliSecondsPerHour * numHours))
    }

    fun showAppOpenAdIfAvailable(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (isShowingAppOpenAd) {
            onAdDismissed()
            return
        }

        if (!isAppOpenAdAvailable()) {
            loadAppOpenAd(activity)
            onAdDismissed()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "App Open Ad dismissed")
                appOpenAd = null
                isShowingAppOpenAd = false
                loadAppOpenAd(activity)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "App Open Ad failed to show: ${adError.message}")
                appOpenAd = null
                isShowingAppOpenAd = false
                loadAppOpenAd(activity)
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAppOpenAd = true
            }
        }

        appOpenAd?.show(activity)
    }

    // ==========================================
    // 2. REWARDED ADS
    // ==========================================
    fun loadRewardedAd(context: Context) {
        if (isLoadingRewardedAd || rewardedAd != null) return
        isLoadingRewardedAd = true

        val request = AdRequest.Builder().build()
        RewardedAd.load(
            context.applicationContext,
            REWARDED_AD_UNIT_ID,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded Ad loaded successfully")
                    rewardedAd = ad
                    isLoadingRewardedAd = false
                    _isRewardedReady.value = true
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Rewarded Ad failed to load: ${loadAdError.message}")
                    isLoadingRewardedAd = false
                    rewardedAd = null
                    _isRewardedReady.value = false
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: (rewardAmount: Int, rewardType: String) -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        val currentAd = rewardedAd
        if (currentAd == null) {
            // If ad is not yet loaded, simulate reward for seamless user testing
            Log.d(TAG, "Rewarded ad was not ready, providing simulated reward fallback")
            onRewardEarned(100, "Coins")
            loadRewardedAd(activity)
            onAdDismissed()
            return
        }

        var rewardGranted = false

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded Ad closed")
                rewardedAd = null
                _isRewardedReady.value = false
                loadRewardedAd(activity)
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Rewarded Ad failed to show: ${adError.message}")
                rewardedAd = null
                _isRewardedReady.value = false
                loadRewardedAd(activity)
                // Fallback grant reward on presentation error so user isn't stuck
                onRewardEarned(100, "Coins")
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded Ad showed full screen")
            }
        }

        currentAd.show(activity) { rewardItem: RewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            rewardGranted = true
            onRewardEarned(rewardItem.amount.coerceAtLeast(100), rewardItem.type.ifBlank { "Coins" })
        }
    }
}

/**
 * Modern Jetpack Compose Banner Ad Component
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    var isAdLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(DarkSlate),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdManager.BANNER_AD_UNIT_ID
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            isAdLoaded = true
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            super.onAdFailedToLoad(error)
                            isAdLoaded = false
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )

        // Fallback placeholder container shown if ad is loading or offline
        if (!isAdLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(GlassBackground, RoundedCornerShape(8.dp))
                    .border(1.dp, GlassBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(Cyan400.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AD",
                            color = Cyan400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google AdMob • Test Banner Ad",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
