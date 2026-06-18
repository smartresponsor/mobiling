package com.smartresponsor.mobile.app.profile

import com.smartresponsor.mobile.client.contract.profile.detail.ProfileDetailPayload
import com.smartresponsor.mobile.client.data.profile.detail.ProfileDetailGateway
import com.smartresponsor.mobile.client.usecase.profile.detail.LoadProfileDetailUseCase

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
