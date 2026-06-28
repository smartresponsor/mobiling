package app.mobiling.client.contract.auth.session

data class AccessResetRecoveryRequest(
    val email: String,
    val code: String,
    val password: String,
)
