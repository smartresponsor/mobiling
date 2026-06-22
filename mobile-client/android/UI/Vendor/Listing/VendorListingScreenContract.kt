package app.mobiling.client.client.ui.vendor.listing

import app.mobiling.client.client.contract.vendor.listing.ListVendorsQuery
import app.mobiling.client.client.contract.vendor.listing.VendorCardSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class VendorListingScreenState(
    val query: ListVendorsQuery,
    val vendors: List<VendorCardSummary>,
    val isLoading: Boolean,
)
