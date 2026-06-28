package app.mobiling.client.data.vendor.transaction

import app.mobiling.client.contract.vendor.transaction.MobileVendorTransactionItemPayload
import app.mobiling.client.contract.vendor.transaction.MobileVendorTransactionPayload
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class HttpVendorTransactionGateway(private val baseUrl: String, private val client: OkHttpClient = OkHttpClient()) : VendorTransactionGateway {
    override suspend fun loadVendorTransaction(vendorId: String): MobileVendorTransactionPayload {
        val request = Request.Builder().url(baseUrl.trimEnd('/') + "/vendor/transaction/" + vendorId).header("Accept", "application/json").get().build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) { throw IllegalStateException("Vendor transaction unavailable.") }
            return payloadFrom(vendorId, body)
        }
    }

    private fun payloadFrom(fallbackVendorId: String, body: String): MobileVendorTransactionPayload {
        val json = JSONObject(body)
        val source = json.optJSONArray("transactions")
        val transactions = (0 until (source?.length() ?: 0)).mapNotNull { index ->
            val item = source?.optJSONObject(index) ?: return@mapNotNull null
            MobileVendorTransactionItemPayload(
                id = text(item, "id"),
                status = text(item, "status"),
                type = text(item, "type"),
                amount = item.optDouble("amount", 0.0),
                currency = text(item, "currency"),
                createdAt = text(item, "createdAt"),
            )
        }
        return MobileVendorTransactionPayload(text(json, "vendorId") ?: fallbackVendorId, transactions)
    }

    private fun text(source: JSONObject, key: String): String? = if (source.isNull(key)) null else source.optString(key).trim().takeIf { it.isNotEmpty() }
}
