package app.mobiling.client.contract.identity.session

data class SessionIdentityPayload(
    val vendorId: String?,
    val accountId: String?,
    val displayName: String?,
    val activeRole: String?,
    val authenticated: Boolean,
    val emailVerified: Boolean,
    val secondFactorEnabled: Boolean,
)
