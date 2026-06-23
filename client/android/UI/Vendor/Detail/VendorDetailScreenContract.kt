package app.mobiling.client.ui.vendor.detail

import app.mobiling.client.contract.vendor.detail.VendorDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class VendorDetailScreenState(
    val vendor: VendorDetailPayload?,
    val isLoading: Boolean,
)
