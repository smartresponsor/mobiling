package com.smartresponsor.mobile.client.contract.auth.session

data class AuthSessionPayload(
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: String,
    val sessionId: String,
)
