package app.mobiling.client.data.vendor.transaction

import app.mobiling.client.contract.vendor.transaction.VendorMobileTransactionPayload

interface VendorTransactionGateway {
    suspend fun loadVendorTransaction(vendorId: String): VendorMobileTransactionPayload
}
