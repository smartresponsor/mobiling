package app.mobiling.client.contract.catalog.browse

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class CatalogNodeSummary(
    val nodeId: String,
    val parentNodeId: String?,
    val title: String,
    val slug: String?,
    val childCount: Int,
    val productCount: Int?,
)
