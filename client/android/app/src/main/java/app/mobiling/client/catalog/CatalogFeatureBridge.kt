package app.mobiling.client.catalog

import app.mobiling.client.contract.catalog.browse.CatalogNodeSummary
import app.mobiling.client.contract.catalog.browse.ListCatalogNodesQuery
import app.mobiling.client.contract.catalog.detail.CatalogNodeDetailPayload
import app.mobiling.client.data.catalog.browse.CatalogBrowseGateway
import app.mobiling.client.data.catalog.detail.CatalogNodeDetailGateway
import app.mobiling.client.usecase.catalog.browse.ListCatalogNodesUseCase
import app.mobiling.client.usecase.catalog.detail.LoadCatalogNodeDetailUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * App-level bridge for catalog-domain controlled rewire.
 * It preserves a stable catalog feature entry point while delegating
 * behavior into canonical Contract/Data/UseCase slices.
 */
class CatalogFeatureBridge(
    private val browseGateway: CatalogBrowseGateway,
    private val detailGateway: CatalogNodeDetailGateway,
) {
    suspend fun list(query: ListCatalogNodesQuery): List<CatalogNodeSummary> =
        ListCatalogNodesUseCase(browseGateway).invoke(query)

    suspend fun detail(nodeId: String): CatalogNodeDetailPayload? =
        LoadCatalogNodeDetailUseCase(detailGateway).invoke(nodeId)
}
