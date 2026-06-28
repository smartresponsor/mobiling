package app.mobiling.client.data.vendor.statement

import app.mobiling.client.contract.vendor.statement.MobileVendorStatementPayload

interface VendorStatementGateway {
    suspend fun loadVendorStatement(vendorId: String): MobileVendorStatementPayload
}
