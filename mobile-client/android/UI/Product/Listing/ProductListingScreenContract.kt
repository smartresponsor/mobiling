package app.mobiling.client.client.ui.product.listing

import app.mobiling.client.client.contract.product.listing.ProductCardSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ProductListingScreenContract {
    fun render(items: List<ProductCardSummary>, isLoading: Boolean)
    fun openDetail(productId: String)
}
