package app.mobiling.client.contract.auth.session

data class RegisterAuthRequest(
    val displayName: String,
    val email: String,
    val password: String,
    val deviceLabel: String?,
)
