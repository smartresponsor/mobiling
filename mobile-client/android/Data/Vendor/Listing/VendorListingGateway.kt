package app.mobiling.client.client.data.vendor.listing

import app.mobiling.client.client.contract.vendor.listing.ListVendorsQuery
import app.mobiling.client.client.contract.vendor.listing.VendorCardSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface VendorListingGateway {
    suspend fun listVendors(query: ListVendorsQuery): List<VendorCardSummary>
}
