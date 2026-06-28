package app.mobiling.client.contract.vendor.payout

data class MobileVendorPayoutPayload(
    val vendorId: String,
    val payoutStatus: String?,
    val currency: String?,
    val availableAmount: Double,
    val pendingAmount: Double,
    val payoutAccountLabel: String?,
)
