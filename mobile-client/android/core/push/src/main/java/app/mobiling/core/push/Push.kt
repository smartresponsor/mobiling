package app.mobiling.core.push

import app.mobiling.client.client.data.system.push.PushTokenRegistrar
import app.mobiling.client.client.usecase.system.push.RegisterPushTokenUseCase

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
