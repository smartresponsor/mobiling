package com.smartresponsor.mobile.client.ui.vendor.listing

import com.smartresponsor.mobile.client.contract.vendor.listing.listvendorsquery
import com.smartresponsor.mobile.client.contract.vendor.listing.vendorcardsummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class VendorListingScreenState(
    val query: ListVendorsQuery,
    val vendors: List<VendorCardSummary>,
    val isLoading: Boolean,
)
