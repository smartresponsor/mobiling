package app.mobiling.client.ui.vendor.statement

import app.mobiling.client.contract.vendor.statement.MobileVendorStatementPayload

data class MobileVendorStatementScreenContract(
    val vendorId: String,
    val statementStatus: String?,
    val currency: String?,
    val grossAmount: Double,
    val netAmount: Double,
) {
    companion object {
        fun from(payload: MobileVendorStatementPayload): MobileVendorStatementScreenContract = MobileVendorStatementScreenContract(
            vendorId = payload.vendorId,
            statementStatus = payload.statementStatus,
            currency = payload.currency,
            grossAmount = payload.grossAmount,
            netAmount = payload.netAmount,
        )
    }
}
