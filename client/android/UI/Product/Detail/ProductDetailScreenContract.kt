package app.mobiling.client.ui.product.detail

import app.mobiling.client.contract.product.detail.ProductDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ProductDetailScreenContract {
    fun render(product: ProductDetailPayload?, isLoading: Boolean)
    fun goBack()
}
