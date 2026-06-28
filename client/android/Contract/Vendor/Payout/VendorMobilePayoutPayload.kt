package app.mobiling.client.contract.vendor.payout

data class VendorMobilePayoutPayload(
    val vendorId: String,
    val payoutStatus: String?,
    val currency: String?,
    val availableAmount: Double,
    val pendingAmount: Double,
    val payoutAccountLabel: String?,
)
