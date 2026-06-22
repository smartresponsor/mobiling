package app.mobiling.client.client.usecase.profile.detail

import app.mobiling.client.client.contract.profile.detail.ProfileDetailPayload
import app.mobiling.client.client.data.profile.detail.ProfileDetailGateway

class LoadProfileDetailUseCase(
    private val gateway: ProfileDetailGateway,
) {
    suspend operator fun invoke(profileId: String): ProfileDetailPayload = gateway.loadProfileDetail(profileId)
}
