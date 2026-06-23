package app.mobiling.client.ui.taxation.detail

import app.mobiling.client.contract.taxation.detail.TaxDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class TaxDetailScreenState(
    val isLoading: Boolean,
    val detail: TaxDetailPayload?,
)
