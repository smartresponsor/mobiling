package com.smartresponsor.mobile.client.usecase.vendor.detail

import com.smartresponsor.mobile.client.data.vendor.detail.VendorDetailGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadVendorDetailUseCase(
    private val gateway: VendorDetailGateway,
) {
    suspend operator fun invoke(vendorId: String) = gateway.loadVendorDetail(vendorId)
}
