package app.mobiling.client.contract.auth.session

data class AuthSessionPayload(
    val status: String,
    val sessionId: String?,
    val authenticated: Boolean,
    val requiresVerification: Boolean,
    val requiresSecondFactor: Boolean,
)
