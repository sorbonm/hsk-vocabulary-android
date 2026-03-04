package info.sorbon.hskvocabulary.core.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import info.sorbon.hskvocabulary.core.common.AdmobAds
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages interstitial ad lifecycle.
 * Matches iOS AdmobManager interstitial pattern:
 * - Preloads ad
 * - Shows when ready
 * - Reloads after dismissal
 */
@Singleton
class InterstitialAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false

    fun preload() {
        if (interstitialAd != null || isLoading) return
        isLoading = true

        InterstitialAd.load(
            context,
            AdmobAds.interstitial,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                }
            }
        )
    }

    /**
     * Shows interstitial if loaded, then preloads next one.
     * @param activity The activity context required for showing ads
     * @param onDismissed Called when ad is dismissed or wasn't available
     */
    fun show(activity: Activity, onDismissed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            preload()
            onDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                preload() // Reload for next time
                onDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                interstitialAd = null
                preload()
                onDismissed()
            }
        }
        ad.show(activity)
    }

    val isReady: Boolean get() = interstitialAd != null
}
