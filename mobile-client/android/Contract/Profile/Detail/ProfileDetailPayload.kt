package com.smartresponsor.mobile.client.contract.profile.detail

data class ProfileDetailPayload(
    val profileId: String,
    val displayName: String?,
    val email: String?,
    val phone: String?,
    val avatarUrl: String?,
)
