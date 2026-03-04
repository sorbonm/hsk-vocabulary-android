package info.sorbon.hskvocabulary.core.util

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class HskFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token will be saved when app is active via UserPreferences
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Deep link handling will be done when user taps the notification
        // and MainActivity receives the intent
    }
}
