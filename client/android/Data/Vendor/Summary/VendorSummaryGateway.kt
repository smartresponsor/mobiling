package app.mobiling.client.data.vendor.summary

import app.mobiling.client.contract.vendor.summary.MobileVendorSummaryPayload

interface VendorSummaryGateway {
    suspend fun loadVendorSummary(vendorId: String): MobileVendorSummaryPayload
}
