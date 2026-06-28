package app.mobiling.client.data.vendor.statement

import app.mobiling.client.contract.vendor.statement.VendorMobileStatementPayload

interface VendorStatementGateway {
    suspend fun loadVendorStatement(vendorId: String): VendorMobileStatementPayload
}
