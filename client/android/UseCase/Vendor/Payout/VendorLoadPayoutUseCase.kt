package app.mobiling.client.usecase.vendor.payout

import app.mobiling.client.contract.vendor.payout.VendorMobilePayoutPayload
import app.mobiling.client.data.vendor.payout.VendorPayoutGateway

class VendorLoadPayoutUseCase(
    private val gateway: VendorPayoutGateway,
) {
    suspend fun load(vendorId: String): VendorMobilePayoutPayload {
        return gateway.loadVendorPayout(vendorId)
    }
}
