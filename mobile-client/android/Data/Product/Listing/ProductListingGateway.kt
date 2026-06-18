package com.smartresponsor.mobile.client.data.product.listing

import com.smartresponsor.mobile.client.contract.product.listing.ProductCardSummary
import com.smartresponsor.mobile.client.contract.product.listing.ProductListQuery

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ProductListingGateway {
    suspend fun listProducts(query: ProductListQuery): List<ProductCardSummary>
}
