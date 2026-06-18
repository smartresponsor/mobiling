package com.smartresponsor.mobile.client.data.catalog.browse

import com.smartresponsor.mobile.client.contract.catalog.browse.catalognodesummary
import com.smartresponsor.mobile.client.contract.catalog.browse.listcatalognodesquery

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface CatalogBrowseGateway {
    suspend fun listNodes(query: ListCatalogNodesQuery): List<CatalogNodeSummary>
}
