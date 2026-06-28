package app.mobiling.client.data.vendor.transaction

import app.mobiling.client.contract.vendor.transaction.MobileVendorTransactionPayload

interface VendorTransactionGateway {
    suspend fun loadVendorTransaction(vendorId: String): MobileVendorTransactionPayload
}
