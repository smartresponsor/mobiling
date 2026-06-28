package app.mobiling.client.contract.auth.session

data class AccessStartAuthRequest(
    val login: String,
    val password: String,
    val deviceLabel: String?,
)
