package app.mobiling.client.client.ui.vendor.detail

import app.mobiling.client.client.contract.vendor.detail.VendorDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class VendorDetailScreenState(
    val vendor: VendorDetailPayload?,
    val isLoading: Boolean,
)
