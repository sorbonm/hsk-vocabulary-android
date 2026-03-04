package info.sorbon.hskvocabulary.core.common

/**
 * AdMob ad unit IDs.
 * iOS IDs are kept as comments for reference; Android needs its own IDs from AdMob console.
 */
object AdmobAds {
    const val BANNER = "ca-app-pub-4956851260856444/9868516346"
    const val INTERSTITIAL = "ca-app-pub-4956851260856444/7575523368"
    const val REWARDED = "ca-app-pub-4956851260856444/7874906269"

    // Test ad units (Google provided, platform-independent)
    const val BANNER_TEST = "ca-app-pub-3940256099942544/6300978111"
    const val INTERSTITIAL_TEST = "ca-app-pub-3940256099942544/1033173712"
    const val INTERSTITIAL_VIDEO_TEST = "ca-app-pub-3940256099942544/8691691433"
    const val REWARDED_TEST = "ca-app-pub-3940256099942544/5224354917"

    /** Use test ads in debug builds */
    val banner: String get() = if (isDebug()) BANNER_TEST else BANNER
    val interstitial: String get() = if (isDebug()) INTERSTITIAL_TEST else INTERSTITIAL
    val rewarded: String get() = if (isDebug()) REWARDED_TEST else REWARDED

    private fun isDebug(): Boolean = info.sorbon.hskvocabulary.BuildConfig.DEBUG
}
