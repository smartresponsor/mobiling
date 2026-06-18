package com.smartresponsor.mobile.client.ui.product.listing

import com.smartresponsor.mobile.client.contract.product.listing.productcardsummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ProductListingScreenContract {
    fun render(items: List<ProductCardSummary>, isLoading: Boolean)
    fun openDetail(productId: String)
}
