package com.smartresponsor.mobile.client.usecase.profile.detail

import com.smartresponsor.mobile.client.contract.profile.detail.ProfileDetailPayload
import com.smartresponsor.mobile.client.data.profile.detail.ProfileDetailGateway

class LoadProfileDetailUseCase(
    private val gateway: ProfileDetailGateway,
) {
    suspend operator fun invoke(profileId: String): ProfileDetailPayload = gateway.loadProfileDetail(profileId)
}
