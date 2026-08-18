package app.mobiling.client.contract.auth.session

data class AccessAuthSessionPayload(
    val status: String,
    val sessionId: String?,
    val vendorId: String?,
    val userUuid: String?,
    val authenticated: Boolean,
    val requiresVerification: Boolean,
    val requiresSecondFactor: Boolean,
)
