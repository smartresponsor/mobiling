package app.mobiling.client.notification

import android.content.Context

class AndroidPushTokenStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun currentToken(): String? = preferences.getString(KEY_TOKEN, null)?.takeIf(String::isNotBlank)

    fun recordToken(token: String): Boolean {
        val normalized = token.trim()
        if (normalized.isEmpty()) return false
        if (currentToken() == normalized) return false
        preferences.edit().putString(KEY_TOKEN, normalized).apply()
        return true
    }

    companion object {
        const val PREFERENCES_NAME = "mobiling.push.lifecycle"
        const val KEY_TOKEN = "token"
    }
}
