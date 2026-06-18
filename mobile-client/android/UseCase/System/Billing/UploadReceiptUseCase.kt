package com.smartresponsor.mobile.client.usecase.system.billing

import com.smartresponsor.mobile.client.contract.system.billing.ReceiptUploadPayload
import com.smartresponsor.mobile.client.data.system.billing.BillingReceiptGateway

class UploadReceiptUseCase(private val billingReceiptGateway: BillingReceiptGateway) {
    operator fun invoke(token: String, product: String): Boolean =
        billingReceiptGateway.upload(ReceiptUploadPayload(token = token, product = product))
}
