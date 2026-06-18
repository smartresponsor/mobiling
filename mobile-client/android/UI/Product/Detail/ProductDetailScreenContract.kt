package com.smartresponsor.mobile.client.ui.product.detail

import com.smartresponsor.mobile.client.contract.product.detail.ProductDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ProductDetailScreenContract {
    fun render(product: ProductDetailPayload?, isLoading: Boolean)
    fun goBack()
}
