package app.mobiling.client.client.contract.auth.session

data class StartAuthRequest(
    val login: String,
    val password: String,
    val deviceLabel: String?,
)
