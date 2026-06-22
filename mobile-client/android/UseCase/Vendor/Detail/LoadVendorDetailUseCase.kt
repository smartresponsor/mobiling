package app.mobiling.client.usecase.vendor.detail

import app.mobiling.client.data.vendor.detail.VendorDetailGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadVendorDetailUseCase(
    private val gateway: VendorDetailGateway,
) {
    suspend operator fun invoke(vendorId: String) = gateway.loadVendorDetail(vendorId)
}
