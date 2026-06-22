package app.mobiling.client.usecase.taxation.detail

import app.mobiling.client.contract.taxation.detail.TaxDetailPayload
import app.mobiling.client.data.taxation.detail.TaxDetailGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadTaxDetailUseCase(
    private val gateway: TaxDetailGateway,
) {
    suspend operator fun invoke(taxDocumentId: String): TaxDetailPayload = gateway.loadTaxDetail(taxDocumentId)
}
