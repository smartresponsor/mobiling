package app.mobiling.core.billing

import app.mobiling.client.contract.system.billing.ReceiptVerificationResult
import app.mobiling.client.data.system.billing.BillingReceiptGateway
import app.mobiling.client.usecase.system.billing.BillingUploadReceiptUseCase
import app.mobiling.client.usecase.system.billing.BillingVerifyReceiptUseCase

/**
 * Legacy-compatible Android entry point bridged to canonical system/billing slices.
 */
class Billing(
    baseUrl: String = "https://httpbin.org",
) {
    private val billingReceiptGateway: BillingReceiptGateway = BillingReceiptGateway(baseUrl)
    private val billingUploadReceiptUseCase: BillingUploadReceiptUseCase = BillingUploadReceiptUseCase(billingReceiptGateway)
    private val billingVerifyReceiptUseCase: BillingVerifyReceiptUseCase = BillingVerifyReceiptUseCase(billingReceiptGateway)

    fun uploadReceipt(token: String, product: String): Boolean = billingUploadReceiptUseCase(token, product)

    fun verifyReceipt(token: String): ReceiptVerificationResult = billingVerifyReceiptUseCase(token)
}
