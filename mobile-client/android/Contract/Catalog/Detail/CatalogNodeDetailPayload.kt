package app.mobiling.client.client.contract.catalog.detail

import app.mobiling.client.client.contract.catalog.browse.CatalogNodeSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class CatalogNodeDetailPayload(
    val node: CatalogNodeSummary,
    val description: String?,
    val breadcrumbLabels: List<String>,
    val featuredProductIds: List<String>,
)
