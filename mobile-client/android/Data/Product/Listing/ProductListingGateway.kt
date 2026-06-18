package com.smartresponsor.mobile.client.data.product.listing

import com.smartresponsor.mobile.client.contract.product.listing.productcardsummary
import com.smartresponsor.mobile.client.contract.product.listing.productlistquery

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ProductListingGateway {
    suspend fun listProducts(query: ProductListQuery): List<ProductCardSummary>
}
