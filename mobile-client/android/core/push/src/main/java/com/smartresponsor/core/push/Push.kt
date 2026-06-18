package com.smartresponsor.core.push

import com.smartresponsor.mobile.client.data.system.push.PushTokenRegistrar
import com.smartresponsor.mobile.client.usecase.system.push.RegisterPushTokenUseCase

/**
 * Legacy-compatible Android entry point bridged to canonical system/push slices.
 */
class Push(
    baseUrl: String = "https://httpbin.org",
) {
    private val pushTokenRegistrar: PushTokenRegistrar = PushTokenRegistrar(baseUrl)
    private val registerPushTokenUseCase: RegisterPushTokenUseCase = RegisterPushTokenUseCase(pushTokenRegistrar)

    fun register(token: String, platform: String = "android"): Boolean =
        registerPushTokenUseCase(token = token, platform = platform)
}
