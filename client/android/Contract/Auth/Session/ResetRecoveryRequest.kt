package app.mobiling.client.contract.auth.session

data class ResetRecoveryRequest(
    val email: String,
    val code: String,
    val password: String,
)
