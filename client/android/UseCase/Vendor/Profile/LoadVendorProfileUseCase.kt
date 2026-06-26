package app.mobiling.client.usecase.vendor.profile

import app.mobiling.client.contract.vendor.profile.MobileVendorProfilePayload
import app.mobiling.client.data.vendor.profile.VendorProfileGateway

class LoadVendorProfileUseCase(
    private val gateway: VendorProfileGateway,
) {
    suspend operator fun invoke(vendorId: String): MobileVendorProfilePayload =
        gateway.loadVendorProfile(vendorId)
}
