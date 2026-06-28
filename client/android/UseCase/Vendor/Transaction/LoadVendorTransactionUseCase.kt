package app.mobiling.client.usecase.vendor.transaction

import app.mobiling.client.contract.vendor.transaction.MobileVendorTransactionPayload
import app.mobiling.client.data.vendor.transaction.VendorTransactionGateway

class LoadVendorTransactionUseCase(
    private val gateway: VendorTransactionGateway,
) {
    suspend fun load(vendorId: String): MobileVendorTransactionPayload {
        return gateway.loadVendorTransaction(vendorId)
    }
}
