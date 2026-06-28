package app.mobiling.client.usecase.vendor.transaction

import app.mobiling.client.contract.vendor.transaction.VendorMobileTransactionPayload
import app.mobiling.client.data.vendor.transaction.VendorTransactionGateway

class VendorLoadTransactionUseCase(
    private val gateway: VendorTransactionGateway,
) {
    suspend fun load(vendorId: String): VendorMobileTransactionPayload {
        return gateway.loadVendorTransaction(vendorId)
    }
}
