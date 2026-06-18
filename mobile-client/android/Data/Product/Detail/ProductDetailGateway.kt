package com.smartresponsor.mobile.client.data.product.detail

import com.smartresponsor.mobile.client.contract.product.detail.ProductDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ProductDetailGateway {
    suspend fun loadProductDetail(productId: String): ProductDetailPayload?
}
