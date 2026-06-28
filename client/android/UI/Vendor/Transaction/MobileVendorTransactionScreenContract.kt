package app.mobiling.client.ui.vendor.transaction

import app.mobiling.client.contract.vendor.transaction.MobileVendorTransactionItemPayload
import app.mobiling.client.contract.vendor.transaction.MobileVendorTransactionPayload

data class MobileVendorTransactionScreenContract(
    val vendorId: String,
    val transactions: List<MobileVendorTransactionItemPayload>,
) {
    companion object {
        fun from(payload: MobileVendorTransactionPayload): MobileVendorTransactionScreenContract = MobileVendorTransactionScreenContract(
            vendorId = payload.vendorId,
            transactions = payload.transactions,
        )
    }
