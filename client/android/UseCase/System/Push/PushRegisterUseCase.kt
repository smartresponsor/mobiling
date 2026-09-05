package app.mobiling.client.usecase.system.push

import app.mobiling.client.contract.system.push.PushRegistrationPayload
import app.mobiling.client.data.system.push.PushRegistrationGateway

class PushRegisterUseCase(private val pushRegistrationGateway: PushRegistrationGateway) {
    operator fun invoke(
        token: String,
        platform: String,
        appKey: String,
        deviceId: String,
        enabled: Boolean = true,
    ): Boolean = pushRegistrationGateway.register(
        PushRegistrationPayload(token = token, platform = platform, appKey = appKey, deviceId = deviceId, enabled = enabled),
    )
}
