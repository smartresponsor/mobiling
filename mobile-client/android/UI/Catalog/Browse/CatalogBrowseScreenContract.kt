package com.smartresponsor.mobile.client.ui.catalog.browse

import com.smartresponsor.mobile.client.contract.catalog.browse.catalognodesummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class CatalogBrowseScreenContract(
    val title: String,
    val nodes: List<CatalogNodeSummary>,
    val isLoading: Boolean,
)
