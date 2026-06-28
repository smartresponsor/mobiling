package app.mobiling.client.data.vendor.profile

import app.mobiling.client.contract.vendor.profile.VendorMobileProfilePayload

interface VendorProfileGateway {
    suspend fun loadVendorProfile(vendorId: String): VendorMobileProfilePayload
}
