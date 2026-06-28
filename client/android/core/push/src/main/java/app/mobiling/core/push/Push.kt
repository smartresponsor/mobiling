package app.mobiling.core.push

import app.mobiling.client.data.system.push.PushRegistrationGateway
import app.mobiling.client.usecase.system.push.PushRegisterUseCase

/**
 * Legacy-compatible Android entry point bridged to canonical system/push slices.
 */
class Push(
    baseUrl: String = "https://httpbin.org",
) {
    private val pushRegistrationGateway: PushRegistrationGateway = PushRegistrationGateway(baseUrl)
    private val pushRegisterUseCase: PushRegisterUseCase = PushRegisterUseCase(pushRegistrationGateway)

    fun register(token: String, platform: String = "android"): Boolean =
        pushRegisterUseCase(token = token, platform = platform)
}
