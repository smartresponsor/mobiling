package com.smartresponsor.mobile.client.ui.catalog.detail

import com.smartresponsor.mobile.client.contract.catalog.detail.CatalogNodeDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class CatalogNodeDetailScreenContract(
    val payload: CatalogNodeDetailPayload?,
    val isLoading: Boolean,
)
