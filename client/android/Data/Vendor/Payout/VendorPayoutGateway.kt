package app.mobiling.client.data.vendor.payout

import app.mobiling.client.contract.vendor.payout.VendorMobilePayoutPayload

interface VendorPayoutGateway {
    suspend fun loadVendorPayout(vendorId: String): VendorMobilePayoutPayload
}
