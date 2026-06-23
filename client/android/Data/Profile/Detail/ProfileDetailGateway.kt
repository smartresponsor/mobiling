package app.mobiling.client.data.profile.detail

import app.mobiling.client.contract.profile.detail.ProfileDetailPayload

interface ProfileDetailGateway {
    suspend fun loadProfileDetail(profileId: String): ProfileDetailPayload
}
