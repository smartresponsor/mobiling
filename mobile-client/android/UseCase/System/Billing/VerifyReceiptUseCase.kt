package com.smartresponsor.mobile.client.usecase.system.billing

import com.smartresponsor.mobile.client.contract.system.billing.receiptverificationresult
import com.smartresponsor.mobile.client.data.system.billing.billingreceiptgateway

class VerifyReceiptUseCase(private val billingReceiptGateway: BillingReceiptGateway) {
    operator fun invoke(token: String): ReceiptVerificationResult =
        billingReceiptGateway.verify(token)
}
