package com.smartresponsor.mobile.client.usecase.product.listing

import com.smartresponsor.mobile.client.contract.product.listing.ProductCardSummary
import com.smartresponsor.mobile.client.contract.product.listing.ProductListQuery
import com.smartresponsor.mobile.client.data.product.listing.ProductListingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListProductsUseCase(
    private val gateway: ProductListingGateway,
) {
    suspend operator fun invoke(query: ProductListQuery): List<ProductCardSummary> = gateway.listProducts(query)
}
