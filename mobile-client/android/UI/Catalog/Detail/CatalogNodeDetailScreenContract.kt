package app.mobiling.client.client.ui.catalog.detail

import app.mobiling.client.client.contract.catalog.detail.CatalogNodeDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class CatalogNodeDetailScreenContract(
    val payload: CatalogNodeDetailPayload?,
    val isLoading: Boolean,
)
