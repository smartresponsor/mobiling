package app.mobiling.core.config

import android.content.Context
import app.mobiling.client.contract.system.config.RemoteConfigPayload
import app.mobiling.client.data.system.config.RemoteConfigStore
import app.mobiling.client.usecase.system.config.RefreshRemoteConfigUseCase

/**
 * Legacy-compatible Android entry point bridged to canonical system/config slices.
 */
class Config(
    private val context: Context,
) {
    private val store: RemoteConfigStore = RemoteConfigStore(context)
    private val refreshRemoteConfigUseCase: RefreshRemoteConfigUseCase = RefreshRemoteConfigUseCase(store)

    suspend fun refresh(
        baseUrl: String = "https://httpbin.org",
        queueMaxAttempt: Int = 3,
        backoffBaseMs: Int = 500,
    ): Boolean = refreshRemoteConfigUseCase(
        baseUrl = baseUrl,
        queueMaxAttempt = queueMaxAttempt,
        backoffBaseMs = backoffBaseMs,
    )

    suspend fun isFresh(ttlSec: Int = 1800): Boolean = store.isFresh(ttlSec)

    fun defaultPayload(): RemoteConfigPayload = store.defaultPayload()
}
