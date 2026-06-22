package app.mobiling.client.contract.catalog.detail

import app.mobiling.client.contract.catalog.browse.CatalogNodeSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class CatalogNodeDetailPayload(
    val node: CatalogNodeSummary,
    val description: String?,
    val breadcrumbLabels: List<String>,
    val featuredProductIds: List<String>,
)
