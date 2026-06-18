package com.smartresponsor.mobile.client.data.catalog.browse

import com.smartresponsor.mobile.client.contract.catalog.browse.CatalogNodeSummary
import com.smartresponsor.mobile.client.contract.catalog.browse.ListCatalogNodesQuery

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface CatalogBrowseGateway {
    suspend fun listNodes(query: ListCatalogNodesQuery): List<CatalogNodeSummary>
}
