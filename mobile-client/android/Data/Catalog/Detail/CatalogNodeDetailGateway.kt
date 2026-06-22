package app.mobiling.client.data.catalog.detail

import app.mobiling.client.contract.catalog.detail.CatalogNodeDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface CatalogNodeDetailGateway {
    suspend fun loadNodeDetail(nodeId: String): CatalogNodeDetailPayload
}
