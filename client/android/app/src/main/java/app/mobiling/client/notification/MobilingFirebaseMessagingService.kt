package app.mobiling.client.notification

import com.google.firebase.messaging.FirebaseMessagingService

class MobilingFirebaseMessagingService : FirebaseMessagingService() {
    @Deprecated("FCM registration tokens are still co-supported during the Firebase Installation ID transition.")
    override fun onNewToken(token: String) {
        if (AndroidPushTokenStore(applicationContext).recordToken(token)) {
            AndroidPushTokenEvents.changed()
        }
    }
}
