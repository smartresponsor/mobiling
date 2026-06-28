package app.mobiling.client.usecase.vendor.summary

import app.mobiling.client.contract.vendor.summary.VendorMobileSummaryPayload
import app.mobiling.client.data.vendor.summary.VendorSummaryGateway

class VendorLoadSummaryUseCase(
    private val gateway: VendorSummaryGateway,
) {
    suspend fun load(vendorId: String): VendorMobileSummaryPayload {
        return gateway.loadVendorSummary(vendorId)
    }
}
