package app.mobiling.client.ui.catalog.browse

import app.mobiling.client.contract.catalog.browse.CatalogNodeSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class CatalogBrowseScreenContract(
    val title: String,
    val nodes: List<CatalogNodeSummary>,
    val isLoading: Boolean,
)
