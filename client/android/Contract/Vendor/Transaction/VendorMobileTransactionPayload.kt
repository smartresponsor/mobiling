package app.mobiling.client.contract.vendor.transaction

data class VendorMobileTransactionPayload(
    val vendorId: String,
    val transactions: List<VendorMobileTransactionItemPayload>,
)

data class VendorMobileTransactionItemPayload(
    val id: String?,
    val status: String?,
    val type: String?,
    val amount: Double,
    val currency: String?,
    val createdAt: String?,
)
