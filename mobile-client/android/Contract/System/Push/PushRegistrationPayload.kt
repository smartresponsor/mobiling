package com.smartresponsor.mobile.client.contract.system.push

data class PushRegistrationPayload(
    val token: String,
    val platform: String = "android",
)
