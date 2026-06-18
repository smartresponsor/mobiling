package com.smartresponsor.core.billing

import com.smartresponsor.mobile.client.contract.system.billing.receiptverificationresult
import com.smartresponsor.mobile.client.data.system.billing.billingreceiptgateway
import com.smartresponsor.mobile.client.usecase.system.billing.uploadreceiptusecase
import com.smartresponsor.mobile.client.usecase.system.billing.verifyreceiptusecase

/**
 * Legacy-compatible Android entry point bridged to canonical system/billing slices.
 */
class Billing(
    baseUrl: String = "https://httpbin.org",
) {
    private val billingReceiptGateway: BillingReceiptGateway = BillingReceiptGateway(baseUrl)
    private val uploadReceiptUseCase: UploadReceiptUseCase = UploadReceiptUseCase(billingReceiptGateway)
    private val verifyReceiptUseCase: VerifyReceiptUseCase = VerifyReceiptUseCase(billingReceiptGateway)

    fun uploadReceipt(token: String, product: String): Boolean = uploadReceiptUseCase(token, product)

    fun verifyReceipt(token: String): ReceiptVerificationResult = verifyReceiptUseCase(token)
}
