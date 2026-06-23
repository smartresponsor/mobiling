package app.mobiling.client.data.product.listing

import app.mobiling.client.contract.product.listing.ProductCardSummary
import app.mobiling.client.contract.product.listing.ProductListQuery

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ProductListingGateway {
    suspend fun listProducts(query: ProductListQuery): List<ProductCardSummary>
}
