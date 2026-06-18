package com.smartresponsor.mobile.client.data.profile.detail

import com.smartresponsor.mobile.client.contract.profile.detail.ProfileDetailPayload

interface ProfileDetailGateway {
    suspend fun loadProfileDetail(profileId: String): ProfileDetailPayload
}
