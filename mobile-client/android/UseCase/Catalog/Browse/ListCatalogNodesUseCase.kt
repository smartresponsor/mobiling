package com.smartresponsor.mobile.client.usecase.catalog.browse

import com.smartresponsor.mobile.client.contract.catalog.browse.CatalogNodeSummary
import com.smartresponsor.mobile.client.contract.catalog.browse.ListCatalogNodesQuery
import com.smartresponsor.mobile.client.data.catalog.browse.CatalogBrowseGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListCatalogNodesUseCase(
    private val gateway: CatalogBrowseGateway,
) {
    suspend operator fun invoke(query: ListCatalogNodesQuery): List<CatalogNodeSummary> = gateway.listNodes(query)
}
