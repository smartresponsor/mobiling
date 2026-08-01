package app.mobiling.client.data.order

import app.mobiling.client.contract.order.OrderMobileItemPayload
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class OrderHttpGateway(private val baseUrl: String, private val client: OkHttpClient = OkHttpClient()) : OrderGateway {
    override suspend fun loadOrders(vendorId: String): List<OrderMobileItemPayload> {
        val array = request("GET", null, null).optJSONArray("items") ?: JSONArray()
        return buildList {
            for (index in 0 until array.length()) array.optJSONObject(index)?.let { item ->
                val id = item.optString("orderId", item.optString("id"))
                add(OrderMobileItemPayload(id, item.optString("reference", item.optString("number", id)), item.optString("status").takeIf(String::isNotBlank), item.optString("totalLabel", item.optString("total")).takeIf(String::isNotBlank)))
            }
        }
    }

    override suspend fun createOrder(fields: Map<String, String>) { request("POST", null, fields) }
    override suspend fun updateOrder(orderId: String, fields: Map<String, String>) { request("PATCH", orderId, fields) }
    override suspend fun deleteOrder(orderId: String) { request("DELETE", orderId, null) }

    private fun request(method: String, identity: String?, fields: Map<String, String>?): JSONObject {
        val url = baseUrl.trimEnd('/') + "/my/order" + (identity?.let { "/$it" } ?: "")
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
            if (!response.isSuccessful) throw IllegalStateException(JSONObject(body.ifBlank { "{}" }).optString("message", "Order CRUD request failed with HTTP ${response.code}."))
            return JSONObject(body.ifBlank { "{}" })
        }
    }
}
