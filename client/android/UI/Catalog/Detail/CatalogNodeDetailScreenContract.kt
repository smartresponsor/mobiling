package app.mobiling.client.ui.catalog.detail

import app.mobiling.client.contract.catalog.detail.CatalogNodeDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class CatalogNodeDetailScreenContract(
    val payload: CatalogNodeDetailPayload?,
    val isLoading: Boolean,
)
