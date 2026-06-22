package app.mobiling.client.product

import app.mobiling.client.client.contract.product.detail.ProductDetailPayload
import app.mobiling.client.client.contract.product.listing.ProductCardSummary
import app.mobiling.client.client.contract.product.listing.ProductListQuery
import app.mobiling.client.client.data.product.detail.ProductDetailGateway
import app.mobiling.client.client.data.product.listing.ProductListingGateway
import app.mobiling.client.client.usecase.product.detail.LoadProductDetailUseCase
import app.mobiling.client.client.usecase.product.listing.ListProductsUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * App-level bridge for product-domain controlled rewire.
 * It preserves a stable product feature entry point while delegating
 * behavior into canonical Contract/Data/UseCase slices.
 */
@Deprecated(
    message = "Prefer VendorBusinessBridge.ownedProduct() or VendorNavigationBridge.product() for vendor-owned navigation.",
    replaceWith = ReplaceWith("vendorBusinessBridge.ownedProduct()")
)
class ProductFeatureBridge(
    private val listingGateway: ProductListingGateway,
    private val detailGateway: ProductDetailGateway,
) {
    suspend fun list(query: ProductListQuery): List<ProductCardSummary> =
        ListProductsUseCase(listingGateway).invoke(query)

    suspend fun detail(productId: String): ProductDetailPayload? =
        LoadProductDetailUseCase(detailGateway).invoke(productId)
}
