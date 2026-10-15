package app.mobiling.client.usecase.system.push

import app.mobiling.client.contract.system.push.PushRegistrationPayload
import app.mobiling.client.data.system.push.PushTokenRegistrar

class RegisterPushTokenUseCase(private val pushTokenRegistrar: PushTokenRegistrar) {
    operator fun invoke(
        token: String,
        platform: String = "android",
        appKey: String,
        deviceId: String,
        enabled: Boolean = true,
    ): Boolean = pushTokenRegistrar.register(
        PushRegistrationPayload(token = token, platform = platform, appKey = appKey, deviceId = deviceId, enabled = enabled),
    )
}
