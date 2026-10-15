package app.mobiling.client.data.product

import app.mobiling.client.contract.product.ProductMobileItemPayload

interface ProductGateway {
    suspend fun loadProducts(vendorId: String): List<ProductMobileItemPayload>
    suspend fun createProduct(fields: Map<String, String>): String
    suspend fun updateProduct(productId: String, fields: Map<String, String>)
    suspend fun deleteProduct(productId: String)
}
