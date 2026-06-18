package com.smartresponsor.mobile.client.usecase.product.listing

import com.smartresponsor.mobile.client.contract.product.listing.productcardsummary
import com.smartresponsor.mobile.client.contract.product.listing.productlistquery
import com.smartresponsor.mobile.client.data.product.listing.productlistinggateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListProductsUseCase(
    private val gateway: ProductListingGateway,
) {
    suspend operator fun invoke(query: ProductListQuery): List<ProductCardSummary> = gateway.listProducts(query)
}
