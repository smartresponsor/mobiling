package app.mobiling.client.ui.vendor.statement

import app.mobiling.client.contract.vendor.statement.VendorMobileStatementPayload

data class VendorMobileStatementScreenContract(
    val vendorId: String,
    val statementStatus: String?,
    val currency: String?,
    val grossAmount: Double,
    val netAmount: Double,
) {
    companion object {
        fun from(payload: VendorMobileStatementPayload): VendorMobileStatementScreenContract = VendorMobileStatementScreenContract(
            vendorId = payload.vendorId,
            statementStatus = payload.statementStatus,
            currency = payload.currency,
            grossAmount = payload.grossAmount,
            netAmount = payload.netAmount,
        )
    }
}
