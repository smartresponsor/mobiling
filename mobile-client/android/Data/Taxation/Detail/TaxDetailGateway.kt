package app.mobiling.client.client.data.taxation.detail

import app.mobiling.client.client.contract.taxation.detail.TaxDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface TaxDetailGateway {
    suspend fun loadTaxDetail(taxDocumentId: String): TaxDetailPayload
}
