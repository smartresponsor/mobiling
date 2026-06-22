package app.mobiling.client.usecase.catalog.detail

import app.mobiling.client.contract.catalog.detail.CatalogNodeDetailPayload
import app.mobiling.client.data.catalog.detail.CatalogNodeDetailGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadCatalogNodeDetailUseCase(
    private val gateway: CatalogNodeDetailGateway,
) {
    suspend operator fun invoke(nodeId: String): CatalogNodeDetailPayload = gateway.loadNodeDetail(nodeId)
}
