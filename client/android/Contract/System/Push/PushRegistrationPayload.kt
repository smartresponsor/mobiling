package app.mobiling.client.contract.system.push

data class PushRegistrationPayload(
    val token: String,
    val platform: String = "android",
    val appKey: String,
    val deviceId: String,
    val enabled: Boolean = true,
)
