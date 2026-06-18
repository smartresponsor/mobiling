package com.smartresponsor.mobile.app.vendor

import com.smartresponsor.mobile.app.profile.ProfileFeatureBridge
import com.smartresponsor.mobile.client.contract.profile.detail.ProfileDetailPayload

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
