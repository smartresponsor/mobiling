package app.mobiling.client.data.vendor.profile

import app.mobiling.client.contract.vendor.profile.MobileVendorProfilePayload

interface VendorProfileGateway {
    suspend fun loadVendorProfile(vendorId: String): MobileVendorProfilePayload
}
