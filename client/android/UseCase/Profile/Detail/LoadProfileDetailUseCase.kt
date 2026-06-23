package app.mobiling.client.usecase.profile.detail

import app.mobiling.client.contract.profile.detail.ProfileDetailPayload
import app.mobiling.client.data.profile.detail.ProfileDetailGateway

class LoadProfileDetailUseCase(
    private val gateway: ProfileDetailGateway,
) {
    suspend operator fun invoke(profileId: String): ProfileDetailPayload = gateway.loadProfileDetail(profileId)
}
