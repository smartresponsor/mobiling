package app.mobiling.client.profile

import app.mobiling.client.contract.profile.detail.ProfileDetailPayload
import app.mobiling.client.data.profile.detail.ProfileDetailGateway
import app.mobiling.client.usecase.profile.detail.LoadProfileDetailUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * App-level bridge for profile-domain controlled rewire.
 */
@Deprecated(
    message = "Prefer VendorBusinessBridge.ownedProfile() or VendorNavigationBridge.profile() for vendor-owned navigation.",
    replaceWith = ReplaceWith("vendorBusinessBridge.ownedProfile()")
)
class ProfileFeatureBridge(
    private val detailGateway: ProfileDetailGateway,
) {
    suspend fun detail(profileId: String): ProfileDetailPayload =
        LoadProfileDetailUseCase(detailGateway).invoke(profileId)
}
