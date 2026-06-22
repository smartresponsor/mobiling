package app.mobiling.client.usecase.catalog.browse

import app.mobiling.client.contract.catalog.browse.CatalogNodeSummary
import app.mobiling.client.contract.catalog.browse.ListCatalogNodesQuery
import app.mobiling.client.data.catalog.browse.CatalogBrowseGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListCatalogNodesUseCase(
    private val gateway: CatalogBrowseGateway,
) {
    suspend operator fun invoke(query: ListCatalogNodesQuery): List<CatalogNodeSummary> = gateway.listNodes(query)
}
