package com.smartresponsor.mobile.client.usecase.catalog.detail

import com.smartresponsor.mobile.client.contract.catalog.detail.catalognodedetailpayload
import com.smartresponsor.mobile.client.data.catalog.detail.catalognodedetailgateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadCatalogNodeDetailUseCase(
    private val gateway: CatalogNodeDetailGateway,
) {
    suspend operator fun invoke(nodeId: String): CatalogNodeDetailPayload = gateway.loadNodeDetail(nodeId)
}
