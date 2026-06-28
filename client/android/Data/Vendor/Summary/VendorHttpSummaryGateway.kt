package app.mobiling.client.data.vendor.summary

import app.mobiling.client.contract.vendor.summary.VendorMobileSummaryPayload
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class VendorHttpSummaryGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) : VendorSummaryGateway {
    override suspend fun loadVendorSummary(vendorId: String): VendorMobileSummaryPayload {
        val request = Request.Builder()
            .url(normalizedBaseUrl() + "/vendor/summary/" + vendorId.encodePathSegment())
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Mobile vendor summary request failed with HTTP ${response.code}.")
            }

            return payloadFrom(vendorId, responseBody)
        }
    }

    private fun payloadFrom(fallbackVendorId: String, responseBody: String): VendorMobileSummaryPayload {
        val json = JSONObject(responseBody)

        return VendorMobileSummaryPayload(
            vendorId = nullableString(json, "vendorId") ?: fallbackVendorId,
            brandName = nullableString(json, "brandName"),
            status = nullableString(json, "status"),
            profileCompletionPercent = json.optInt("profileCompletionPercent", 0).coerceIn(0, 100),
            nextAction = nullableString(json, "nextAction"),
        )
    }

    private fun nullableString(source: JSONObject, key: String): String? =
        if (source.isNull(key)) null else source.optString(key).trim().takeIf { it.isNotEmpty() }

    private fun normalizedBaseUrl(): String = baseUrl.trimEnd('/')

    private fun String.encodePathSegment(): String =
        java.net.URLEncoder.encode(this, Charsets.UTF_8.name()).replace("+", "%20")
}
