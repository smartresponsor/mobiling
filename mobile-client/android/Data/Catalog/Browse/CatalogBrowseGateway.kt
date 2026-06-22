package app.mobiling.client.data.catalog.browse

import app.mobiling.client.contract.catalog.browse.CatalogNodeSummary
import app.mobiling.client.contract.catalog.browse.ListCatalogNodesQuery

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface CatalogBrowseGateway {
    suspend fun listNodes(query: ListCatalogNodesQuery): List<CatalogNodeSummary>
}
