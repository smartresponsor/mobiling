package app.mobiling.client.contract.catalog.browse

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ListCatalogNodesQuery(
    val parentNodeId: String?,
    val searchText: String?,
    val includeEmptyNodes: Boolean,
)
