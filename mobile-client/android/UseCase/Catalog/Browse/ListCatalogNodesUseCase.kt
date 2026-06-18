package com.smartresponsor.mobile.client.usecase.catalog.browse

import com.smartresponsor.mobile.client.contract.catalog.browse.catalognodesummary
import com.smartresponsor.mobile.client.contract.catalog.browse.listcatalognodesquery
import com.smartresponsor.mobile.client.data.catalog.browse.catalogbrowsegateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListCatalogNodesUseCase(
    private val gateway: CatalogBrowseGateway,
) {
    suspend operator fun invoke(query: ListCatalogNodesQuery): List<CatalogNodeSummary> = gateway.listNodes(query)
}
