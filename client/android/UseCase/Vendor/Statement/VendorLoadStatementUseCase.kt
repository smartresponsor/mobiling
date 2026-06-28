package app.mobiling.client.usecase.vendor.statement

import app.mobiling.client.contract.vendor.statement.VendorMobileStatementPayload
import app.mobiling.client.data.vendor.statement.VendorStatementGateway

class VendorLoadStatementUseCase(
    private val gateway: VendorStatementGateway,
) {
    suspend fun load(vendorId: String): VendorMobileStatementPayload {
        return gateway.loadVendorStatement(vendorId)
    }
}
