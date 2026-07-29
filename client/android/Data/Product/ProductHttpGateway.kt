package app.mobiling.client.data.product

import app.mobiling.client.contract.product.ProductMobileItemPayload
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class ProductHttpGateway(private val baseUrl: String, private val client: OkHttpClient = OkHttpClient()) : ProductGateway {
    override suspend fun loadProducts(vendorId: String): List<ProductMobileItemPayload> {
        val root = request("GET", null, null)
        val array = root.optJSONArray("items") ?: JSONArray()
        return buildList {
            for (index in 0 until array.length()) array.optJSONObject(index)?.let { item ->
                val id = item.optString("productId", item.optString("id"))
                add(ProductMobileItemPayload(id, item.optString("title", item.optString("name", "Product")), item.optString("status").takeIf(String::isNotBlank), item.optString("priceLabel", item.optString("price")).takeIf(String::isNotBlank)))
            }
        }
    }

    override suspend fun createProduct(fields: Map<String, String>) { request("POST", null, fields) }
    override suspend fun updateProduct(productId: String, fields: Map<String, String>) { request("PATCH", productId, fields) }
    override suspend fun deleteProduct(productId: String) { request("DELETE", productId, null) }

    private fun request(method: String, identity: String?, fields: Map<String, String>?): JSONObject {
        val url = baseUrl.trimEnd('/') + "/crud/my/product" + (identity?.let { "/$it" } ?: "")
        val payload = fields?.let { JSONObject(it).toString().toRequestBody("application/json".toMediaType()) }
        val builder = Request.Builder().url(url).header("Accept", "application/json")
        when (method) {
            "POST" -> builder.post(requireNotNull(payload))
            "PATCH" -> builder.patch(requireNotNull(payload))
            "DELETE" -> builder.delete()
            else -> builder.get()
        }
        client.newCall(builder.build()).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IllegalStateException(JSONObject(body.ifBlank { "{}" }).optString("message", "Product CRUD request failed with HTTP ${response.code}."))
            return JSONObject(body.ifBlank { "{}" })
        }
    }
}
