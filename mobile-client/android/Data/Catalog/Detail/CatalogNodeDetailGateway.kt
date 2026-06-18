package com.smartresponsor.mobile.client.data.catalog.detail

import com.smartresponsor.mobile.client.contract.catalog.detail.catalognodedetailpayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface CatalogNodeDetailGateway {
    suspend fun loadNodeDetail(nodeId: String): CatalogNodeDetailPayload
}
