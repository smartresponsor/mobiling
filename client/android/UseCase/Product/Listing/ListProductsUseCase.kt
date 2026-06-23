package app.mobiling.client.usecase.product.listing

import app.mobiling.client.contract.product.listing.ProductCardSummary
import app.mobiling.client.contract.product.listing.ProductListQuery
import app.mobiling.client.data.product.listing.ProductListingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListProductsUseCase(
    private val gateway: ProductListingGateway,
) {
    suspend operator fun invoke(query: ProductListQuery): List<ProductCardSummary> = gateway.listProducts(query)
}
