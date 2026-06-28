package app.mobiling.client.ui.vendor.payout

import app.mobiling.client.contract.vendor.payout.VendorMobilePayoutPayload

data class VendorMobilePayoutScreenContract(
    val vendorId: String,
    val payoutStatus: String?,
    val currency: String?,
    val availableAmount: Double,
    val pendingAmount: Double,
    val payoutAccountLabel: String?,
) {
    companion object {
        fun from(payload: VendorMobilePayoutPayload): VendorMobilePayoutScreenContract = VendorMobilePayoutScreenContract(
            vendorId = payload.vendorId,
            payoutStatus = payload.payoutStatus,
            currency = payload.currency,
            availableAmount = payload.availableAmount,
            pendingAmount = payload.pendingAmount,
            payoutAccountLabel = payload.payoutAccountLabel,
        )
    }
