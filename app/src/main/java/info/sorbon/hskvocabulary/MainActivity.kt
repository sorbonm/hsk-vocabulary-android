package info.sorbon.hskvocabulary

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import info.sorbon.hskvocabulary.core.ads.ConsentManager
import info.sorbon.hskvocabulary.core.ads.InterstitialAdManager
import info.sorbon.hskvocabulary.core.ads.RewardedAdManager
import info.sorbon.hskvocabulary.core.datastore.UserPreferences
import info.sorbon.hskvocabulary.core.util.AppUpdateManager
import info.sorbon.hskvocabulary.data.remote.firebase.AuthDataSource
import info.sorbon.hskvocabulary.data.remote.firebase.FirestoreDataSource
import info.sorbon.hskvocabulary.presentation.navigation.HskNavHost
import info.sorbon.hskvocabulary.presentation.navigation.Screen
import info.sorbon.hskvocabulary.presentation.theme.HskVocabularyTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var authDataSource: AuthDataSource
    @Inject lateinit var firestoreDataSource: FirestoreDataSource
    @Inject lateinit var consentManager: ConsentManager
    @Inject lateinit var interstitialAdManager: InterstitialAdManager
    @Inject lateinit var rewardedAdManager: RewardedAdManager
    @Inject lateinit var appUpdateManager: AppUpdateManager

    private val showUpdateDialog = mutableStateOf(false)
    private val updateVersion = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val isPreloaded = runBlocking {
            userPreferences.isWordsPreloaded.first()
        }

        val startDestination = if (isPreloaded) Screen.Home.route else Screen.Preload.route

        // Anonymous sign-in + initial profile creation (matching iOS pattern)
        lifecycleScope.launch { ensureSignedIn() }

        // Subscribe to push notifications topic
        FirebaseMessaging.getInstance().subscribeToTopic("news")

        // Gather GDPR consent, then preload ads
        consentManager.gatherConsent(this) {
            interstitialAdManager.preload()
            rewardedAdManager.preload()
        }

        // Check for app updates
        appUpdateManager.checkForUpdate(this) { version ->
            updateVersion.value = version
            showUpdateDialog.value = true
        }

        setContent {
            HskVocabularyTheme {
                val navController = rememberNavController()
                HskNavHost(
                    navController = navController,
                    startDestination = startDestination,
                    interstitialAdManager = interstitialAdManager,
                    rewardedAdManager = rewardedAdManager
                )

                if (showUpdateDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showUpdateDialog.value = false },
                        title = { Text("Update Available") },
                        text = { Text("A new version (${updateVersion.value}) is available. Would you like to update?") },
                        confirmButton = {
                            TextButton(onClick = {
                                showUpdateDialog.value = false
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                            }) { Text("Update") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showUpdateDialog.value = false }) { Text("Later") }
                        }
                    )
                }
            }
        }
    }

    /**
     * Ensures the user is anonymously signed in and has a Firestore profile.
     * Matches iOS AuthManager + FirestoreManager.createInitialUserProfile.
     */
    private suspend fun ensureSignedIn() {
        try {
            val uid = if (authDataSource.isSignedIn) {
                authDataSource.currentUserId!!
            } else {
                authDataSource.signInAnonymously()
            }
            val nickname = firestoreDataSource.generateNickname(uid)
            val country = Locale.getDefault().country
            firestoreDataSource.createInitialUserProfile(uid, nickname, country)
        } catch (_: Exception) {
            // Non-critical: will retry next launch
        }
    }
}
