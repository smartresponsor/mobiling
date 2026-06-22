package app.mobiling.client.client.data.profile.detail

import app.mobiling.client.client.contract.profile.detail.ProfileDetailPayload

interface ProfileDetailGateway {
    suspend fun loadProfileDetail(profileId: String): ProfileDetailPayload
}
