package app.mobiling.client.usecase.vendor.listing

import app.mobiling.client.contract.vendor.listing.ListVendorsQuery
import app.mobiling.client.data.vendor.listing.VendorListingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListVendorsUseCase(
    private val gateway: VendorListingGateway,
) {
    suspend operator fun invoke(query: ListVendorsQuery) = gateway.listVendors(query)
}
