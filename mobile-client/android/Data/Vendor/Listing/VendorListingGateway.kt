package app.mobiling.client.data.vendor.listing

import app.mobiling.client.contract.vendor.listing.ListVendorsQuery
import app.mobiling.client.contract.vendor.listing.VendorCardSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface VendorListingGateway {
    suspend fun listVendors(query: ListVendorsQuery): List<VendorCardSummary>
}
