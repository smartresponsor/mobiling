package com.smartresponsor.mobile.client.usecase.profile.detail

import com.smartresponsor.mobile.client.contract.profile.detail.profiledetailpayload
import com.smartresponsor.mobile.client.data.profile.detail.profiledetailgateway

class LoadProfileDetailUseCase(
    private val gateway: ProfileDetailGateway,
) {
    suspend operator fun invoke(profileId: String): ProfileDetailPayload = gateway.loadProfileDetail(profileId)
}
