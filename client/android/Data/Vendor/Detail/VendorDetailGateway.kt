package app.mobiling.client.data.vendor.detail

import app.mobiling.client.contract.vendor.detail.VendorDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface VendorDetailGateway {
    suspend fun loadVendorDetail(vendorId: String): VendorDetailPayload
}
