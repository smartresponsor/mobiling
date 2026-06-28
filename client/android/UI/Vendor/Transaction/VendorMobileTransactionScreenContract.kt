package app.mobiling.client.ui.vendor.transaction

import app.mobiling.client.contract.vendor.transaction.VendorMobileTransactionItemPayload
import app.mobiling.client.contract.vendor.transaction.VendorMobileTransactionPayload

data class VendorMobileTransactionScreenContract(
    val vendorId: String,
    val transactions: List<VendorMobileTransactionItemPayload>,
) {
    companion object {
        fun from(payload: VendorMobileTransactionPayload): VendorMobileTransactionScreenContract = VendorMobileTransactionScreenContract(
            vendorId = payload.vendorId,
            transactions = payload.transactions,
        )
    }
