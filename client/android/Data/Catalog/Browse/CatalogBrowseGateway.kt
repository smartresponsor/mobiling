package app.mobiling.client.data.catalog.browse

import app.mobiling.client.contract.catalog.browse.CatalogNodeSummary
import app.mobiling.client.contract.catalog.browse.CatalogListNodeQuery

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface CatalogBrowseGateway {
    suspend fun listNodes(query: CatalogListNodeQuery): List<CatalogNodeSummary>
}
