package app.mobiling.client.data.vendor.payout

import app.mobiling.client.contract.vendor.payout.MobileVendorPayoutPayload

interface VendorPayoutGateway {
    suspend fun loadVendorPayout(vendorId: String): MobileVendorPayoutPayload
}
