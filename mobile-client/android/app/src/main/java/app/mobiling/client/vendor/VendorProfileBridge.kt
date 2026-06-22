package app.mobiling.client.vendor

import app.mobiling.client.profile.ProfileFeatureBridge
import app.mobiling.client.client.contract.profile.detail.ProfileDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Vendor-owned profile bridge.
 */
class VendorProfileBridge(
    private val feature: ProfileFeatureBridge,
) {
    suspend fun detail(profileId: String): ProfileDetailPayload = feature.detail(profileId)

    fun feature(): ProfileFeatureBridge = feature
}
