package info.sorbon.hskvocabulary.core.util

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import info.sorbon.hskvocabulary.BuildConfig
import info.sorbon.hskvocabulary.data.remote.firebase.RemoteConfigDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateManager @Inject constructor(
    private val remoteConfigDataSource: RemoteConfigDataSource
) {

    fun checkForUpdate(activity: Activity, onSoftUpdate: (latestVersion: String) -> Unit) {
        val currentVersion = parseVersion(BuildConfig.VERSION_NAME)
        val minVersion = parseVersion(remoteConfigDataSource.minVersion)
        val latestVersion = parseVersion(remoteConfigDataSource.latestVersion)

        when {
            // Force update: current version is below minimum
            currentVersion < minVersion -> startImmediateUpdate(activity)
            // Soft update: newer version available
            currentVersion < latestVersion -> onSoftUpdate(remoteConfigDataSource.latestVersion)
        }
    }

    private fun startImmediateUpdate(activity: Activity) {
        val appUpdateManager = AppUpdateManagerFactory.create(activity)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlow(
                    info,
                    activity,
                    AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
                )
            }
        }.addOnFailureListener { e ->
            Log.w(TAG, "In-app update check failed: ${e.message}")
        }
    }

    private fun parseVersion(version: String): List<Int> {
        return version.split(".").mapNotNull { it.toIntOrNull() }
    }

    private operator fun List<Int>.compareTo(other: List<Int>): Int {
        for (i in 0 until maxOf(size, other.size)) {
            val a = getOrElse(i) { 0 }
            val b = other.getOrElse(i) { 0 }
            if (a != b) return a.compareTo(b)
        }
        return 0
    }

    companion object {
        private const val TAG = "AppUpdateManager"
    }
}
