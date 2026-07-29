package app.mobiling.client.usecase.product

import app.mobiling.client.contract.product.ProductMobileItemPayload
import app.mobiling.client.data.product.ProductGateway

class ProductLoadUseCase(private val gateway: ProductGateway) { suspend fun load(vendorId: String): List<ProductMobileItemPayload> = gateway.loadProducts(vendorId) }
