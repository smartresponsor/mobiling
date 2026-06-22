package app.mobiling.client.contract.auth.session

data class AuthSessionPayload(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: String,
    val sessionId: String,
)
