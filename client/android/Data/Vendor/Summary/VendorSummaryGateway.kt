package app.mobiling.client.data.vendor.summary

import app.mobiling.client.contract.vendor.summary.VendorMobileSummaryPayload

interface VendorSummaryGateway {
    suspend fun loadVendorSummary(vendorId: String): VendorMobileSummaryPayload
}
