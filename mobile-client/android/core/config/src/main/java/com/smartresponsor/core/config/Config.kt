package com.smartresponsor.core.config

import android.content.Context
import com.smartresponsor.mobile.client.contract.system.config.remoteconfigpayload
import com.smartresponsor.mobile.client.data.system.config.remoteconfigstore
import com.smartresponsor.mobile.client.usecase.system.config.refreshremoteconfigusecase

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
