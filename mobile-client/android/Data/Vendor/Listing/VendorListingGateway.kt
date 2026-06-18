package com.smartresponsor.mobile.client.data.vendor.listing

import com.smartresponsor.mobile.client.contract.vendor.listing.ListVendorsQuery
import com.smartresponsor.mobile.client.contract.vendor.listing.VendorCardSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface VendorListingGateway {
    suspend fun listVendors(query: ListVendorsQuery): List<VendorCardSummary>
}
