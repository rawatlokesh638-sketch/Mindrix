package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

object AdManager {
    private const val TAG = "MindrixAdManager"

    // Ad Unit IDs - KEEP UNCHANGED
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-2330525119428447/8776498040"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-2330525119428447/8536824897"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-2330525119428447/7271844687"

    // Configuration
    private const val APP_OPEN_CACHE_DURATION_HOURS = 4L
    private const val APP_OPEN_COOLDOWN_HOURS = 4L
    private const val MAX_RETRY_COUNT = 5
    private const val BASE_RETRY_DELAY_MS = 30000L
    private const val MAX_RETRY_DELAY_MS = 120000L

    // Initialization
    private var isInitialized = false
    
    // App Open Ad State
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAppOpenAd = false
    private var appOpenLoadTime: Long = 0
    private var lastAppOpenShowTime: Long = 0
    private var isAppOpenAdShowing = false
    private var appOpenRetryCount = 0
    private var appOpenRetryRunnable: Runnable? = null

    // Rewarded Ad State
    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewardedAd = false
    private val _isRewardedReady = MutableStateFlow(false)
    val isRewardedReady = _isRewardedReady.asStateFlow()
    
    private var isRewardInProgress = false
    private var isRewardGranted = false
    private var isRewardedAdShowing = false
    private var pendingRewardCallback: ((RewardType) -> Unit)? = null
    private var pendingDismissCallback: (() -> Unit)? = null
    private var rewardedRetryCount = 0
    private var rewardedRetryRunnable: Runnable? = null

    // Banner Ad State
    private var bannerAdView: AdView? = null
    private var isBannerLoading = false
    
    // Lifecycle State
    private var isAppInBackground = false
    private var isAppFirstLaunch = true
    private var isGameplayActive = false
    private var isLoginActive = false
    private var isAnyFullscreenAdShowing = false

    // Reward Types - App-defined rewards
    enum class RewardType {
        REVIVE,
        COINS_2X,
        XP_BOOST,
        EXTRA_BRAIN_SYNC,
        DAILY_BONUS
    }

    // ==========================================
    // INITIALIZATION
    // ==========================================
    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob initialized: $status")
                isInitialized = true
                loadAppOpenAd(context)
                loadRewardedAd(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds", e)
        }
    }

    // ==========================================
    // APP OPEN ADS
    // ==========================================
    fun loadAppOpenAd(context: Context) {
        // Prevent duplicate loads
        if (isLoadingAppOpenAd) {
            Log.d(TAG, "App Open Ad already loading")
            return
        }
        
        if (appOpenAd != null && isAppOpenAdAvailable()) {
            Log.d(TAG, "App Open Ad already available")
            return
        }

        isLoadingAppOpenAd = true
        Log.d(TAG, "Loading App Open Ad")

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
                    appOpenRetryCount = 0
                    
                    // Cancel pending retry
                    appOpenRetryRunnable?.let {
                        android.os.Handler().removeCallbacks(it)
                        appOpenRetryRunnable = null
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "App Open Ad failed to load: ${loadAdError.message}")
                    isLoadingAppOpenAd = false
                    appOpenAd = null
                    
                    scheduleAppOpenRetry(context)
                }
            }
        )
    }

    private fun scheduleAppOpenRetry(context: Context) {
        if (appOpenRetryCount >= MAX_RETRY_COUNT) {
            Log.w(TAG, "App Open Ad max retries reached")
            return
        }

        // Cancel existing retry
        appOpenRetryRunnable?.let {
            android.os.Handler().removeCallbacks(it)
            appOpenRetryRunnable = null
        }

        appOpenRetryCount++
        val delayMs = min(BASE_RETRY_DELAY_MS * appOpenRetryCount, MAX_RETRY_DELAY_MS)
        Log.d(TAG, "Scheduling App Open retry $appOpenRetryCount in ${delayMs}ms")
        
        appOpenRetryRunnable = Runnable {
            if (!isAppInBackground && !isAppOpenAdShowing && !isAnyFullscreenAdShowing) {
                loadAppOpenAd(context)
            }
        }
        android.os.Handler().postDelayed(appOpenRetryRunnable!!, delayMs)
    }

    private fun isAppOpenAdAvailable(): Boolean {
        if (appOpenAd == null) return false
        
        val now = Date().time
        val cacheDuration = TimeUnit.HOURS.toMillis(APP_OPEN_CACHE_DURATION_HOURS)
        val isCacheValid = (now - appOpenLoadTime) < cacheDuration
        
        val cooldownDuration = TimeUnit.HOURS.toMillis(APP_OPEN_COOLDOWN_HOURS)
        val isCooldownValid = (now - lastAppOpenShowTime) >= cooldownDuration
        
        return isCacheValid && isCooldownValid
    }

    fun showAppOpenAdIfAvailable(
        activity: Activity,
        onAdDismissed: () -> Unit = {}
    ) {
        // Comprehensive checks before showing
        if (isAppOpenAdShowing) {
            Log.d(TAG, "App Open Ad already showing")
            onAdDismissed()
            return
        }
        
        if (isAnyFullscreenAdShowing) {
            Log.d(TAG, "Another fullscreen ad is showing")
            onAdDismissed()
            return
        }
        
        if (isAppFirstLaunch) {
            Log.d(TAG, "Skipping App Open Ad - first launch")
            onAdDismissed()
            return
        }
        
        if (isGameplayActive) {
            Log.d(TAG, "Skipping App Open Ad - gameplay active")
            onAdDismissed()
            return
        }
        
        if (isLoginActive) {
            Log.d(TAG, "Skipping App Open Ad - login active")
            onAdDismissed()
            return
        }
        
        if (isAppInBackground) {
            Log.d(TAG, "Skipping App Open Ad - app in background")
            onAdDismissed()
            return
        }

        if (!isAppOpenAdAvailable()) {
            Log.d(TAG, "App Open Ad not available")
            onAdDismissed()
            return
        }

        val ad = appOpenAd
        if (ad == null) {
            Log.d(TAG, "App Open Ad is null")
            onAdDismissed()
            return
        }

        Log.d(TAG, "Showing App Open Ad")
        isAppOpenAdShowing = true
        isAnyFullscreenAdShowing = true

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "App Open Ad dismissed")
                appOpenAd = null
                isAppOpenAdShowing = false
                isAnyFullscreenAdShowing = false
                lastAppOpenShowTime = Date().time
                
                // Preload next ad
                if (!isAppInBackground) {
                    loadAppOpenAd(activity)
                }
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "App Open Ad failed to show: ${adError.message}")
                appOpenAd = null
                isAppOpenAdShowing = false
                isAnyFullscreenAdShowing = false
                
                if (!isAppInBackground) {
                    loadAppOpenAd(activity)
                }
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "App Open Ad showed")
            }
        }

        try {
            ad.show(activity)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing App Open Ad", e)
            isAppOpenAdShowing = false
            isAnyFullscreenAdShowing = false
            appOpenAd = null
            if (!isAppInBackground) {
                loadAppOpenAd(activity)
            }
            onAdDismissed()
        }
    }

    // ==========================================
    // REWARDED ADS - NO COOLDOWN, NO DAILY LIMIT
    // ==========================================
    fun loadRewardedAd(context: Context) {
        // Prevent duplicate loads
        if (isLoadingRewardedAd) {
            Log.d(TAG, "Rewarded Ad already loading")
            return
        }
        
        if (rewardedAd != null) {
            Log.d(TAG, "Rewarded Ad already available")
            return
        }

        isLoadingRewardedAd = true
        Log.d(TAG, "Loading Rewarded Ad")

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
                    rewardedRetryCount = 0
                    
                    // Cancel pending retry
                    rewardedRetryRunnable?.let {
                        android.os.Handler().removeCallbacks(it)
                        rewardedRetryRunnable = null
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Rewarded Ad failed to load: ${loadAdError.message}")
                    isLoadingRewardedAd = false
                    rewardedAd = null
                    _isRewardedReady.value = false
                    
                    scheduleRewardedRetry(context)
                }
            }
        )
    }

    private fun scheduleRewardedRetry(context: Context) {
        if (rewardedRetryCount >= MAX_RETRY_COUNT) {
            Log.w(TAG, "Rewarded Ad max retries reached")
            return
        }

        // Cancel existing retry
        rewardedRetryRunnable?.let {
            android.os.Handler().removeCallbacks(it)
            rewardedRetryRunnable = null
        }

        rewardedRetryCount++
        val delayMs = min(BASE_RETRY_DELAY_MS * rewardedRetryCount, MAX_RETRY_DELAY_MS)
        Log.d(TAG, "Scheduling Rewarded retry $rewardedRetryCount in ${delayMs}ms")
        
        rewardedRetryRunnable = Runnable {
            if (!isAppInBackground && !isRewardedAdShowing && !isAnyFullscreenAdShowing) {
                loadRewardedAd(context)
            }
        }
        android.os.Handler().postDelayed(rewardedRetryRunnable!!, delayMs)
    }

    fun showRewardedAd(
        activity: Activity,
        rewardType: RewardType,
        onRewardEarned: (RewardType) -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        // Prevent duplicate shows
        if (isRewardInProgress) {
            Log.d(TAG, "Reward already in progress")
            onAdDismissed()
            return
        }
        
        if (isRewardedAdShowing) {
            Log.d(TAG, "Rewarded Ad already showing")
            onAdDismissed()
            return
        }
        
        if (isAnyFullscreenAdShowing) {
            Log.d(TAG, "Another fullscreen ad is showing")
            onAdDismissed()
            return
        }

        val currentAd = rewardedAd
        if (currentAd == null) {
            Log.d(TAG, "Rewarded ad not ready - NO REWARD, preloading")
            loadRewardedAd(activity)
            onAdDismissed()
            return
        }

        // Set up callbacks
        pendingRewardCallback = onRewardEarned
        pendingDismissCallback = onAdDismissed
        isRewardInProgress = true
        isRewardGranted = false
        isRewardedAdShowing = true
        isAnyFullscreenAdShowing = true

        Log.d(TAG, "Showing Rewarded Ad for reward type: $rewardType")

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded Ad dismissed - Reward granted: $isRewardGranted")
                rewardedAd = null
                _isRewardedReady.value = false
                isRewardInProgress = false
                isRewardedAdShowing = false
                isAnyFullscreenAdShowing = false
                
                // Preload next ad
                loadRewardedAd(activity)
                
                // Only call dismiss if reward wasn't granted
                if (!isRewardGranted) {
                    pendingDismissCallback?.invoke()
                }
                pendingRewardCallback = null
                pendingDismissCallback = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Rewarded Ad failed to show: ${adError.message}")
                rewardedAd = null
                _isRewardedReady.value = false
                isRewardInProgress = false
                isRewardedAdShowing = false
                isAnyFullscreenAdShowing = false
                isRewardGranted = false
                
                // Preload next ad
                loadRewardedAd(activity)
                
                // NO REWARD on failure
                pendingDismissCallback?.invoke()
                pendingRewardCallback = null
                pendingDismissCallback = null
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded Ad showed full screen")
            }
        }

        try {
            currentAd.show(activity) { rewardItem: RewardItem ->
                // Grant reward ONLY from real callback, exactly once
                if (!isRewardGranted) {
                    isRewardGranted = true
                    Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                    
                    // Pass only RewardType - app handles its own reward logic
                    pendingRewardCallback?.invoke(rewardType)
                    pendingRewardCallback = null
                } else {
                    Log.w(TAG, "Duplicate reward callback ignored")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing Rewarded Ad", e)
            isRewardInProgress = false
            isRewardedAdShowing = false
            isAnyFullscreenAdShowing = false
            isRewardGranted = false
            rewardedAd = null
            _isRewardedReady.value = false
            loadRewardedAd(activity)
            
            // NO REWARD on exception
            pendingDismissCallback?.invoke()
            pendingRewardCallback = null
            pendingDismissCallback = null
        }
    }

    // ==========================================
    // BANNER ADS - Clean creation/destruction
    // ==========================================
    fun createBannerAdView(context: Context): AdView {
        // Always create a fresh AdView to avoid parent/screen reuse issues
        return AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = BANNER_AD_UNIT_ID
            visibility = android.view.View.GONE
            
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    isBannerLoading = false
                    visibility = android.view.View.VISIBLE
                    Log.d(TAG, "Banner ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isBannerLoading = false
                    visibility = android.view.View.GONE
                    Log.w(TAG, "Banner ad failed to load: ${error.message}")
                }
            }
            
            // Start loading immediately
            loadAd(AdRequest.Builder().build())
        }
    }

    fun loadBannerAd(context: Context) {
        if (isBannerLoading) return
        
        // Destroy any existing banner
        destroyBannerAd()
        
        isBannerLoading = true
        bannerAdView = createBannerAdView(context)
    }

    fun destroyBannerAd() {
        bannerAdView?.destroy()
        bannerAdView = null
        isBannerLoading = false
    }

    // ==========================================
    // LIFECYCLE MANAGEMENT
    // ==========================================
    fun onAppForeground(context: Context) {
        isAppInBackground = false
        
        if (isAppFirstLaunch) {
            isAppFirstLaunch = false
        }
        
        // Reload ads if needed
        if (!isAppOpenAdShowing && !isAnyFullscreenAdShowing) {
            loadAppOpenAd(context)
            loadRewardedAd(context)
        }
    }

    fun onAppBackground() {
        isAppInBackground = true
        // Clean up banner to save memory
        destroyBannerAd()
    }

    fun setGameplayActive(active: Boolean) {
        isGameplayActive = active
    }

    fun setLoginActive(active: Boolean) {
        isLoginActive = active
    }

    fun isFullscreenAdShowing(): Boolean = isAnyFullscreenAdShowing

    // ==========================================
    // CLEANUP
    // ==========================================
    fun cleanup() {
        // Cancel retry runnables
        appOpenRetryRunnable?.let {
            android.os.Handler().removeCallbacks(it)
            appOpenRetryRunnable = null
        }
        rewardedRetryRunnable?.let {
            android.os.Handler().removeCallbacks(it)
            rewardedRetryRunnable = null
        }
        
        // Clean up ads
        appOpenAd = null
        rewardedAd = null
        destroyBannerAd()
        
        // Reset states
        isLoadingAppOpenAd = false
        isLoadingRewardedAd = false
        isAppOpenAdShowing = false
        isRewardedAdShowing = false
        isAnyFullscreenAdShowing = false
        isRewardInProgress = false
        isRewardGranted = false
        _isRewardedReady.value = false
        
        pendingRewardCallback = null
        pendingDismissCallback = null
        
        Log.d(TAG, "AdManager cleaned up")
    }
}

// ==========================================
// COMPOSE BANNER AD VIEW - Safe, no reuse issues
// ==========================================
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var adView by remember { mutableStateOf<AdView?>(null) }
    var isAdLoaded by remember { mutableStateOf(false) }
    var shouldShowAd by remember { mutableStateOf(true) }

    // Create banner when composable enters composition
    LaunchedEffect(Unit) {
        val view = AdManager.createBannerAdView(context)
        adView = view
    }

    // Clean up when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            adView?.destroy()
            adView = null
        }
    }

    // Only show if banner is loaded
    if (shouldShowAd && adView != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.Transparent)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    adView ?: AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = AdManager.BANNER_AD_UNIT_ID
                    }
                },
                update = { view ->
                    // Update if needed
                }
            )
        }
    }
}        return appOpenAd != null && (dateDifference < (numMilliSecondsPerHour * numHours))
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
