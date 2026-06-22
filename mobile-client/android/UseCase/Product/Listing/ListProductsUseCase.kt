package app.mobiling.client.client.usecase.product.listing

import app.mobiling.client.client.contract.product.listing.ProductCardSummary
import app.mobiling.client.client.contract.product.listing.ProductListQuery
import app.mobiling.client.client.data.product.listing.ProductListingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListProductsUseCase(
    private val gateway: ProductListingGateway,
) {
    suspend operator fun invoke(query: ProductListQuery): List<ProductCardSummary> = gateway.listProducts(query)
}
