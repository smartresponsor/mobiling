package app.mobiling.client.vendor

import app.mobiling.client.client.contract.vendor.detail.VendorDetailPayload
import app.mobiling.client.client.contract.vendor.listing.ListVendorsQuery
import app.mobiling.client.client.contract.vendor.listing.VendorCardSummary
import app.mobiling.client.client.data.vendor.detail.VendorDetailGateway
import app.mobiling.client.client.data.vendor.listing.VendorListingGateway
import app.mobiling.client.client.usecase.vendor.detail.LoadVendorDetailUseCase
import app.mobiling.client.client.usecase.vendor.listing.ListVendorsUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * App-level bridge for vendor-domain controlled rewire.
 * It preserves a stable vendor feature entry point while delegating
 * behavior into canonical Contract/Data/UseCase slices.
 */
class VendorFeatureBridge(
    private val listingGateway: VendorListingGateway,
    private val detailGateway: VendorDetailGateway,
) {
    suspend fun list(query: ListVendorsQuery): List<VendorCardSummary> =
        ListVendorsUseCase(listingGateway).invoke(query)

    suspend fun detail(vendorId: String): VendorDetailPayload? =
        LoadVendorDetailUseCase(detailGateway).invoke(vendorId)
}
