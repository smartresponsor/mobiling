package app.mobiling.client.client.data.system.config

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.mobiling.client.client.contract.system.config.RemoteConfigPayload
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request

private val Context.remoteConfigDataStore by preferencesDataStore(name = "rc")

class RemoteConfigStore(private val context: Context) {
    suspend fun refresh(
        baseUrl: String = "https://httpbin.org",
        queueMaxAttempt: Int = 3,
        backoffBaseMs: Int = 500,
    ): Boolean {
        var attempt = 0
        while (attempt < queueMaxAttempt) {
            attempt++
            val response = OkHttpClient()
                .newCall(
                    Request.Builder()
                        .url("$baseUrl/anything/mobile/config")
                        .build(),
                )
                .execute()

            if (response.isSuccessful) {
                val payload = defaultPayloadJson()
                context.remoteConfigDataStore.edit {
                    it[stringPreferencesKey("payload")] = payload
                    it[longPreferencesKey("fetchedAt")] = System.currentTimeMillis()
                }
                return true
            }

            delay((backoffBaseMs * attempt).toLong())
        }

        return false
    }

    suspend fun isFresh(ttlSec: Int = 1800): Boolean {
        val fetchedAt = context.remoteConfigDataStore.data.first()[longPreferencesKey("fetchedAt")] ?: return false
        return System.currentTimeMillis() - fetchedAt < ttlSec * 1000L
    }

    fun defaultPayload(): RemoteConfigPayload = RemoteConfigPayload(
        ttlSec = 1800,
        analyticEnabled = true,
        billingGraceDay = 3,
        securitySigningEnabled = true,
    )

    private fun defaultPayloadJson(): String =
        """{"ttlSec":1800,"flag":{"analyticEnabled":true,"billing.graceDay":3,"security.signingEnabled":true}}"""
}
