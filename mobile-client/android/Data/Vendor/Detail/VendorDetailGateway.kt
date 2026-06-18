package com.smartresponsor.mobile.client.data.vendor.detail

import com.smartresponsor.mobile.client.contract.vendor.detail.vendordetailpayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface VendorDetailGateway {
    suspend fun loadVendorDetail(vendorId: String): VendorDetailPayload
}
