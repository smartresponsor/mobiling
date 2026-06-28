package app.mobiling.client.usecase.vendor.payout

import app.mobiling.client.contract.vendor.payout.MobileVendorPayoutPayload
import app.mobiling.client.data.vendor.payout.VendorPayoutGateway

class LoadVendorPayoutUseCase(
    private val gateway: VendorPayoutGateway,
) {
    suspend fun load(vendorId: String): MobileVendorPayoutPayload {
        return gateway.loadVendorPayout(vendorId)
    }
}
