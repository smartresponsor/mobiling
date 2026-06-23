package app.mobiling.client.usecase.product.detail

import app.mobiling.client.contract.product.detail.ProductDetailPayload
import app.mobiling.client.data.product.detail.ProductDetailGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadProductDetailUseCase(
    private val gateway: ProductDetailGateway,
) {
    suspend operator fun invoke(productId: String): ProductDetailPayload? = gateway.loadProductDetail(productId)
}
