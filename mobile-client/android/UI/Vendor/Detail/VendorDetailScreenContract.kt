package com.smartresponsor.mobile.client.ui.vendor.detail

import com.smartresponsor.mobile.client.contract.vendor.detail.VendorDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class VendorDetailScreenState(
    val vendor: VendorDetailPayload?,
    val isLoading: Boolean,
)
