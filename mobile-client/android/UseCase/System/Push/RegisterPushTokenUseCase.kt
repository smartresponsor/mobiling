package com.smartresponsor.mobile.client.usecase.system.push

import com.smartresponsor.mobile.client.contract.system.push.pushregistrationpayload
import com.smartresponsor.mobile.client.data.system.push.pushtokenregistrar

class RegisterPushTokenUseCase(private val pushTokenRegistrar: PushTokenRegistrar) {
    operator fun invoke(token: String, platform: String = "android"): Boolean =
        pushTokenRegistrar.register(PushRegistrationPayload(token = token, platform = platform))
}
