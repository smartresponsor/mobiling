package app.mobiling.client.usecase.vendor.summary

import app.mobiling.client.contract.vendor.summary.MobileVendorSummaryPayload
import app.mobiling.client.data.vendor.summary.VendorSummaryGateway

class LoadVendorSummaryUseCase(
    private val gateway: VendorSummaryGateway,
) {
    suspend fun load(vendorId: String): MobileVendorSummaryPayload {
        return gateway.loadVendorSummary(vendorId)
    }
}
