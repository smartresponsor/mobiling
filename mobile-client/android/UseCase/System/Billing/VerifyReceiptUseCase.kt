package app.mobiling.client.client.usecase.system.billing

import app.mobiling.client.client.contract.system.billing.ReceiptVerificationResult
import app.mobiling.client.client.data.system.billing.BillingReceiptGateway

class VerifyReceiptUseCase(private val billingReceiptGateway: BillingReceiptGateway) {
    operator fun invoke(token: String): ReceiptVerificationResult =
        billingReceiptGateway.verify(token)
}
