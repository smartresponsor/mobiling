package app.mobiling.client.contract.vendor.summary

data class MobileVendorSummaryPayload(
    val vendorId: String,
    val brandName: String?,
    val status: String?,
    val profileCompletionPercent: Int,
    val nextAction: String?,
)
