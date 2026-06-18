package com.smartresponsor.mobile.client.ui.taxation.detail

import com.smartresponsor.mobile.client.contract.taxation.detail.TaxDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class TaxDetailScreenState(
    val isLoading: Boolean,
    val detail: TaxDetailPayload?,
)
