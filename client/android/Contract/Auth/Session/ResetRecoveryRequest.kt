package app.mobiling.client.contract.auth.session

data class ResetRecoveryRequest(
    val code: String,
    val password: String,
)
