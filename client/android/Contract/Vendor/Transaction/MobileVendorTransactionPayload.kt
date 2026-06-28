package app.mobiling.client.contract.vendor.transaction

data class MobileVendorTransactionPayload(
    val vendorId: String,
    val transactions: List<MobileVendorTransactionItemPayload>,
)

data class MobileVendorTransactionItemPayload(
    val id: String?,
    val status: String?,
    val type: String?,
    val amount: Double,
    val currency: String?,
    val createdAt: String?,
)
