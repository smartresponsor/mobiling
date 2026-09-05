package app.mobiling.core.push

import app.mobiling.client.data.system.push.PushRegistrationGateway
import app.mobiling.client.usecase.system.push.PushRegisterUseCase

/**
 * Legacy-compatible Android entry point bridged to canonical system/push slices.
 */
class Push(
    baseUrl: String,
) {
    private val pushRegistrationGateway: PushRegistrationGateway = PushRegistrationGateway(baseUrl)
    private val pushRegisterUseCase: PushRegisterUseCase = PushRegisterUseCase(pushRegistrationGateway)

    fun register(
        token: String,
        platform: String = "android",
        appKey: String,
        deviceId: String,
        enabled: Boolean = true,
    ): Boolean = pushRegisterUseCase(
        token = token,
        platform = platform,
        appKey = appKey,
        deviceId = deviceId,
        enabled = enabled,
    )
}
