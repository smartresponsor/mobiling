package app.mobiling.client.contract.vendor.statement

data class VendorMobileStatementPayload(
    val vendorId: String,
    val statementStatus: String?,
    val currency: String?,
    val grossAmount: Double,
    val netAmount: Double,
)
