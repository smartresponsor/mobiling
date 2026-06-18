package com.smartresponsor.mobile.app.vendor

import com.smartresponsor.mobile.app.product.ProductFeatureBridge
import com.smartresponsor.mobile.client.contract.product.detail.ProductDetailPayload
import com.smartresponsor.mobile.client.contract.product.listing.ProductCardSummary
import com.smartresponsor.mobile.client.contract.product.listing.ProductListQuery

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Vendor-owned product bridge.
 *
 * This wrapper keeps Product reachable through Vendor ownership semantics
 * instead of encouraging direct flat app-level access.
 */
class VendorProductBridge(
    private val feature: ProductFeatureBridge,
) {
    suspend fun list(query: ProductListQuery): List<ProductCardSummary> = feature.list(query)

    suspend fun detail(productId: String): ProductDetailPayload? = feature.detail(productId)

    fun feature(): ProductFeatureBridge = feature
}
