package com.smartresponsor.mobile.client.contract.catalog.detail

import com.smartresponsor.mobile.client.contract.catalog.browse.CatalogNodeSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class CatalogNodeDetailPayload(
    val node: CatalogNodeSummary,
    val description: String?,
    val breadcrumbLabels: List<String>,
    val featuredProductIds: List<String>,
)
