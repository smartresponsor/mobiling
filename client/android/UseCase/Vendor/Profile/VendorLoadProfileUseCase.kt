package app.mobiling.client.usecase.vendor.profile

import app.mobiling.client.contract.vendor.profile.VendorMobileProfilePayload
import app.mobiling.client.data.vendor.profile.VendorProfileGateway

class VendorLoadProfileUseCase(
    private val gateway: VendorProfileGateway,
) {
    suspend operator fun invoke(vendorId: String): VendorMobileProfilePayload =
        gateway.loadVendorProfile(vendorId)
}
