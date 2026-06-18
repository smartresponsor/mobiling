package com.smartresponsor.mobile.app.vendor

import com.smartresponsor.mobile.client.contract.vendor.detail.VendorDetailPayload
import com.smartresponsor.mobile.client.contract.vendor.listing.ListVendorsQuery
import com.smartresponsor.mobile.client.contract.vendor.listing.VendorCardSummary
import com.smartresponsor.mobile.client.data.vendor.detail.VendorDetailGateway
import com.smartresponsor.mobile.client.data.vendor.listing.VendorListingGateway
import com.smartresponsor.mobile.client.usecase.vendor.detail.LoadVendorDetailUseCase
import com.smartresponsor.mobile.client.usecase.vendor.listing.ListVendorsUseCase

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
