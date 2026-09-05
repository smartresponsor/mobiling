package app.mobiling.client.data.product

import app.mobiling.client.contract.product.ProductMobileItemPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    override suspend fun createProduct(fields: Map<String, String>): String {
        val root = request("POST", null, fields)
        val item = root.optJSONObject("item") ?: JSONObject()
        val identity = item.optString("id", item.optString("retailId")).trim()
        if (identity.isBlank()) {
            throw IllegalStateException("Retail create response did not include an identity.")
        }
        return identity
    }
    override suspend fun updateProduct(productId: String, fields: Map<String, String>) { request("PATCH", productId, fields) }
    override suspend fun deleteProduct(productId: String) { request("DELETE", productId, null) }

    private suspend fun request(method: String, identity: String?, fields: Map<String, String>?): JSONObject = withContext(Dispatchers.IO) {
        val url = baseUrl.trimEnd('/') + "/my/retail" + (identity?.let { "/$it" } ?: "")
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
            JSONObject(body.ifBlank { "{}" })
        }
    }
}
