package app.mobiling.client.usecase.vendor.statement

import app.mobiling.client.contract.vendor.statement.MobileVendorStatementPayload
import app.mobiling.client.data.vendor.statement.VendorStatementGateway

class LoadVendorStatementUseCase(
    private val gateway: VendorStatementGateway,
) {
    suspend fun load(vendorId: String): MobileVendorStatementPayload {
        return gateway.loadVendorStatement(vendorId)
    }
}
