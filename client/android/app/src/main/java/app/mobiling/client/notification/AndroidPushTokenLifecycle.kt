package app.mobiling.client.notification

import android.content.Context
import app.mobiling.client.contract.notification.NotificationSubscriptionRequest
import java.util.UUID

class AndroidPushTokenLifecycle(context: Context) {
    private val preferences = context.getSharedPreferences(AndroidPushTokenStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val tokenStore = AndroidPushTokenStore(context)

    val installationId: String
        get() = preferences.getString(KEY_INSTALLATION_ID, null)?.takeIf(String::isNotBlank)
            ?: UUID.randomUUID().toString().also { value ->
                preferences.edit().putString(KEY_INSTALLATION_ID, value).apply()
            }

    fun recordToken(token: String) {
        if (tokenStore.recordToken(token)) {
            AndroidPushTokenEvents.changed()
        }
    }

    suspend fun sync(bridge: NotificationFeatureBridge?, appKey: String): Boolean {
        val token = tokenStore.currentToken() ?: return false
        val resolvedBridge = bridge ?: return false
        val ok = resolvedBridge.subscription(
            NotificationSubscriptionRequest(
                token = token,
                platform = "android",
                appKey = appKey,
                deviceId = installationId,
                enabled = true,
            ),
        )
        if (ok) preferences.edit().putString(KEY_REGISTERED_TOKEN, token).apply()
        return ok
    }

    suspend fun disable(bridge: NotificationFeatureBridge?, appKey: String): Boolean {
        val token = preferences.getString(KEY_REGISTERED_TOKEN, null)
            ?: tokenStore.currentToken()
            ?: return false
        val resolvedBridge = bridge ?: return false
        val ok = resolvedBridge.subscription(
            NotificationSubscriptionRequest(
                token = token,
                platform = "android",
                appKey = appKey,
                deviceId = installationId,
                enabled = false,
            ),
        )
        if (ok) preferences.edit().remove(KEY_REGISTERED_TOKEN).apply()
        return ok
    }

    companion object {
        val tokenChanges = AndroidPushTokenEvents.changes

        private const val KEY_INSTALLATION_ID = "installation_id"
        private const val KEY_REGISTERED_TOKEN = "registered_token"

        fun recordProviderToken(context: Context, token: String) {
            AndroidPushTokenLifecycle(context.applicationContext).recordToken(token)
        }
    }
}
