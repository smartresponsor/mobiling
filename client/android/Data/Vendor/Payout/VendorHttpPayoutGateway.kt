package app.mobiling.client.data.vendor.payout

import app.mobiling.client.contract.vendor.payout.VendorMobilePayoutPayload
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class VendorHttpPayoutGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) : VendorPayoutGateway {
    override suspend fun loadVendorPayout(vendorId: String): VendorMobilePayoutPayload {
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/vendor/payout/" + vendorId)
            .header("Accept", "application/json")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) { throw IllegalStateException("Vendor payout unavailable.") }
            return payloadFrom(vendorId, body)
        }
    }

    private fun payloadFrom(fallbackVendorId: String, body: String): VendorMobilePayoutPayload {
        val json = JSONObject(body)
        return VendorMobilePayoutPayload(
            vendorId = text(json, "vendorId") ?: fallbackVendorId,
            payoutStatus = text(json, "payoutStatus"),
            currency = text(json, "currency"),
            availableAmount = json.optDouble("availableAmount", 0.0),
            pendingAmount = json.optDouble("pendingAmount", 0.0),
            payoutAccountLabel = text(json, "payoutAccountLabel"),
        )
    }

    private fun text(source: JSONObject, key: String): String? =
        if (source.isNull(key)) null else source.optString(key).trim().takeIf { it.isNotEmpty() }
}
