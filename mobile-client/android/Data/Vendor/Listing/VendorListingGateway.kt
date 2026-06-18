package com.smartresponsor.mobile.client.data.vendor.listing

import com.smartresponsor.mobile.client.contract.vendor.listing.listvendorsquery
import com.smartresponsor.mobile.client.contract.vendor.listing.vendorcardsummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface VendorListingGateway {
    suspend fun listVendors(query: ListVendorsQuery): List<VendorCardSummary>
}
