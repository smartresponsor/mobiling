package app.mobiling.client.data.product.detail

import app.mobiling.client.contract.product.detail.ProductDetailPayload

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ProductDetailGateway {
    suspend fun loadProductDetail(productId: String): ProductDetailPayload?
}
