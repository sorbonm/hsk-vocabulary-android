package info.sorbon.hskvocabulary.data.remote.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigDataSource @Inject constructor() {

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

    init {
        remoteConfig.setDefaultsAsync(DEFAULTS)
        remoteConfig.fetchAndActivate()
    }

    val leaderboardLimit: Long
        get() = remoteConfig.getLong(KEY_LEADERBOARD_LIMIT)

    val quizMaxDurationSeconds: Long
        get() = remoteConfig.getLong(KEY_QUIZ_MAX_DURATION)

    val latestVersion: String
        get() = remoteConfig.getString(KEY_ANDROID_LATEST_VERSION)

    val minVersion: String
        get() = remoteConfig.getString(KEY_ANDROID_MIN_VERSION)

    companion object {
        private const val KEY_LEADERBOARD_LIMIT = "leaderboard_limit"
        private const val KEY_QUIZ_MAX_DURATION = "quiz_max_duration_seconds"
        private const val KEY_ANDROID_LATEST_VERSION = "android_latest_version"
        private const val KEY_ANDROID_MIN_VERSION = "android_min_version"

        private val DEFAULTS = mapOf<String, Any>(
            KEY_LEADERBOARD_LIMIT to 50L,
            KEY_QUIZ_MAX_DURATION to 600L,
            KEY_ANDROID_LATEST_VERSION to "1.0.0",
            KEY_ANDROID_MIN_VERSION to "1.0.0"
        )
    }
}
