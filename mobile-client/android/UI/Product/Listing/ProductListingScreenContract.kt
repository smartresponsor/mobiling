package app.mobiling.client.ui.product.listing

import app.mobiling.client.contract.product.listing.ProductCardSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ProductListingScreenContract {
    fun render(items: List<ProductCardSummary>, isLoading: Boolean)
    fun openDetail(productId: String)
}
