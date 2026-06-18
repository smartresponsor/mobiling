package com.smartresponsor.mobile.client.usecase.product.detail

import com.smartresponsor.mobile.client.contract.product.detail.productdetailpayload
import com.smartresponsor.mobile.client.data.product.detail.productdetailgateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadProductDetailUseCase(
    private val gateway: ProductDetailGateway,
) {
    suspend operator fun invoke(productId: String): ProductDetailPayload? = gateway.loadProductDetail(productId)
}
