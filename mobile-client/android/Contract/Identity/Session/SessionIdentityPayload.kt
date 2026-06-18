package com.smartresponsor.mobile.client.contract.identity.session

data class SessionIdentityPayload(
    val userId: String,
    val accountId: String,
    val displayName: String?,
    val activeRole: String?,
    val authenticated: Boolean,
)
