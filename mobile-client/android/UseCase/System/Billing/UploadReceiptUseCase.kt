package com.smartresponsor.mobile.client.usecase.system.billing

import com.smartresponsor.mobile.client.contract.system.billing.receiptuploadpayload
import com.smartresponsor.mobile.client.data.system.billing.billingreceiptgateway

class UploadReceiptUseCase(private val billingReceiptGateway: BillingReceiptGateway) {
    operator fun invoke(token: String, product: String): Boolean =
        billingReceiptGateway.upload(ReceiptUploadPayload(token = token, product = product))
}
