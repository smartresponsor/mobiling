package com.smartresponsor.mobile.client.usecase.system.config

import com.smartresponsor.mobile.client.data.system.config.RemoteConfigStore

class RefreshRemoteConfigUseCase(private val remoteConfigStore: RemoteConfigStore) {
    suspend operator fun invoke(
        baseUrl: String = "https://httpbin.org",
        queueMaxAttempt: Int = 3,
        backoffBaseMs: Int = 500,
    ): Boolean = remoteConfigStore.refresh(
        baseUrl = baseUrl,
        queueMaxAttempt = queueMaxAttempt,
        backoffBaseMs = backoffBaseMs,
    )
}
