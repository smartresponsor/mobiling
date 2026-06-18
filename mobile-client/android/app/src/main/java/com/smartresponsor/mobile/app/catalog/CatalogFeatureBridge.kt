package com.smartresponsor.mobile.app.catalog

import com.smartresponsor.mobile.client.contract.catalog.browse.CatalogNodeSummary
import com.smartresponsor.mobile.client.contract.catalog.browse.ListCatalogNodesQuery
import com.smartresponsor.mobile.client.contract.catalog.detail.CatalogNodeDetailPayload
import com.smartresponsor.mobile.client.data.catalog.browse.CatalogBrowseGateway
import com.smartresponsor.mobile.client.data.catalog.detail.CatalogNodeDetailGateway
import com.smartresponsor.mobile.client.usecase.catalog.browse.ListCatalogNodesUseCase
import com.smartresponsor.mobile.client.usecase.catalog.detail.LoadCatalogNodeDetailUseCase

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
