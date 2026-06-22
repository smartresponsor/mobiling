package app.mobiling.client.client.usecase.catalog.detail

import app.mobiling.client.client.contract.catalog.detail.CatalogNodeDetailPayload
import app.mobiling.client.client.data.catalog.detail.CatalogNodeDetailGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadCatalogNodeDetailUseCase(
    private val gateway: CatalogNodeDetailGateway,
) {
    suspend operator fun invoke(nodeId: String): CatalogNodeDetailPayload = gateway.loadNodeDetail(nodeId)
}
