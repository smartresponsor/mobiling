package com.smartresponsor.mobile.client.usecase.vendor.listing

import com.smartresponsor.mobile.client.contract.vendor.listing.listvendorsquery
import com.smartresponsor.mobile.client.data.vendor.listing.vendorlistinggateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListVendorsUseCase(
    private val gateway: VendorListingGateway,
) {
    suspend operator fun invoke(query: ListVendorsQuery) = gateway.listVendors(query)
}
