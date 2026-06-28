package app.mobiling.client.ui.vendor.summary

import app.mobiling.client.contract.vendor.summary.MobileVendorSummaryPayload

data class MobileVendorSummaryScreenContract(
    val vendorId: String,
    val title: String,
    val brandName: String?,
    val status: String?,
    val profileCompletionPercent: Int,
    val nextAction: String?,
) {
    companion object {
        fun from(payload: MobileVendorSummaryPayload): MobileVendorSummaryScreenContract =
            MobileVendorSummaryScreenContract(
                vendorId = payload.vendorId,
                title = payload.brandName ?: "Vendor Summary",
                brandName = payload.brandName,
                status = payload.status,
                profileCompletionPercent = payload.profileCompletionPercent,
                nextAction = payload.nextAction,
            )
    }
}
