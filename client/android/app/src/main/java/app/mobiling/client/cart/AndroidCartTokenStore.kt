package app.mobiling.client.cart

import android.content.Context
import app.mobiling.client.data.cart.CartTokenStore

class AndroidCartTokenStore(context: Context) : CartTokenStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun current(): String? = preferences.getString(KEY_TOKEN, null)?.trim()?.takeIf(String::isNotEmpty)

    override fun save(token: String) {
        val normalized = token.trim()
        if (normalized.isEmpty()) {
            preferences.edit().remove(KEY_TOKEN).apply()
        } else {
            preferences.edit().putString(KEY_TOKEN, normalized).apply()
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "mobiling_cart"
        const val KEY_TOKEN = "cart_token"
    }
}
