package app.mobiling.client.vendor

import app.mobiling.client.product.ProductFeatureBridge
import app.mobiling.client.client.contract.product.detail.ProductDetailPayload
import app.mobiling.client.client.contract.product.listing.ProductCardSummary
import app.mobiling.client.client.contract.product.listing.ProductListQuery

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
