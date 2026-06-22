package app.mobiling.client.client.usecase.system.push

import app.mobiling.client.client.contract.system.push.PushRegistrationPayload
import app.mobiling.client.client.data.system.push.PushTokenRegistrar

class RegisterPushTokenUseCase(private val pushTokenRegistrar: PushTokenRegistrar) {
    operator fun invoke(token: String, platform: String = "android"): Boolean =
        pushTokenRegistrar.register(PushRegistrationPayload(token = token, platform = platform))
}
