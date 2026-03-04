package info.sorbon.hskvocabulary.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import info.sorbon.hskvocabulary.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConsentManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val isMobileAdsInitialized = AtomicBoolean(false)
    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    fun gatherConsent(activity: Activity, onAdsReady: () -> Unit) {
        val params = if (BuildConfig.DEBUG) {
            val debugSettings = ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("TEST-DEVICE-HASHED-ID")
                .build()
            ConsentRequestParameters.Builder()
                .setConsentDebugSettings(debugSettings)
                .build()
        } else {
            ConsentRequestParameters.Builder().build()
        }

        // Fast path: if consent already obtained, init ads immediately
        if (consentInformation.canRequestAds()) {
            initMobileAdsAndPreload(onAdsReady)
        }

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error: ${formError.message}")
                    }
                    if (consentInformation.canRequestAds()) {
                        initMobileAdsAndPreload(onAdsReady)
                    }
                }
            },
            { requestError ->
                Log.w(TAG, "Consent info update failed: ${requestError.message}")
                // On error, still try to load ads if previously consented
                if (consentInformation.canRequestAds()) {
                    initMobileAdsAndPreload(onAdsReady)
                }
            }
        )
    }

    private fun initMobileAdsAndPreload(onAdsReady: () -> Unit) {
        if (isMobileAdsInitialized.getAndSet(true)) return
        MobileAds.initialize(context) { onAdsReady() }
    }

    companion object {
        private const val TAG = "ConsentManager"
    }
}
