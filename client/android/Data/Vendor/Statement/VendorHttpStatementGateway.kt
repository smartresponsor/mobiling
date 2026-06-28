package app.mobiling.client.data.vendor.statement

import app.mobiling.client.contract.vendor.statement.VendorMobileStatementPayload
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class VendorHttpStatementGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) : VendorStatementGateway {
    override suspend fun loadVendorStatement(vendorId: String): VendorMobileStatementPayload {
        val request = Request.Builder()
            .url(normalizedBaseUrl() + "/vendor/statement/" + vendorId.encodePathSegment())
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("Mobile vendor statement request failed with HTTP ${response.code}.")
            }
            return payloadFrom(vendorId, responseBody)
        }
    }

    private fun payloadFrom(fallbackVendorId: String, responseBody: String): VendorMobileStatementPayload {
        val json = JSONObject(responseBody)
        return VendorMobileStatementPayload(
            vendorId = nullableString(json, "vendorId") ?: fallbackVendorId,
            statementStatus = nullableString(json, "statementStatus"),
            currency = nullableString(json, "currency"),
            grossAmount = json.optDouble("grossAmount", 0.0),
            netAmount = json.optDouble("netAmount", 0.0),
        )
    }

    private fun nullableString(source: JSONObject, key: String): String? =
        if (source.isNull(key)) null else source.optString(key).trim().takeIf { it.isNotEmpty() }

    private fun normalizedBaseUrl(): String = baseUrl.trimEnd('/')

    private fun String.encodePathSegment(): String =
        java.net.URLEncoder.encode(this, Charsets.UTF_8.name()).replace("+", "%20")
}
