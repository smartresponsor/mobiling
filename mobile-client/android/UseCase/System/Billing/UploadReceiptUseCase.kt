package app.mobiling.client.client.usecase.system.billing

import app.mobiling.client.client.contract.system.billing.ReceiptUploadPayload
import app.mobiling.client.client.data.system.billing.BillingReceiptGateway

class UploadReceiptUseCase(private val billingReceiptGateway: BillingReceiptGateway) {
    operator fun invoke(token: String, product: String): Boolean =
        billingReceiptGateway.upload(ReceiptUploadPayload(token = token, product = product))
}
