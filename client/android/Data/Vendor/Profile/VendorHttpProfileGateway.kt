package app.mobiling.client.data.vendor.profile

import app.mobiling.client.contract.vendor.profile.VendorMobileProfilePayload
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class VendorHttpProfileGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) : VendorProfileGateway {
    override suspend fun loadVendorProfile(vendorId: String): VendorMobileProfilePayload {
        val request = Request.Builder()
            .url(normalizedBaseUrl() + "/vendor/profile/" + vendorId.encodePathSegment())
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(errorMessage(responseBody, response.code))
            }

            return payloadFrom(vendorId, responseBody)
        }
    }

    private fun payloadFrom(fallbackVendorId: String, responseBody: String): VendorMobileProfilePayload {
        val json = JSONObject(responseBody)

        return VendorMobileProfilePayload(
            vendorId = nullableString(json, "vendorId") ?: fallbackVendorId,
            displayName = nullableString(json, "displayName"),
            brandName = nullableString(json, "brandName"),
            status = nullableString(json, "status"),
            completionPercent = json.optInt("completionPercent", 0).coerceIn(0, 100),
            readyForPublishing = json.optBoolean("readyForPublishing", false),
            nextAction = nullableString(json, "nextAction"),
            avatarUrl = nullableString(json, "avatarUrl"),
            coverUrl = nullableString(json, "coverUrl"),
            about = nullableString(json, "about"),
            website = nullableString(json, "website"),
            publicationStatus = nullableString(json, "publicationStatus"),
        )
    }

    private fun nullableString(source: JSONObject, key: String): String? {
        if (source.isNull(key)) {
            return null
        }

        return source.optString(key).trim().takeIf { it.isNotEmpty() }
    }

    private fun errorMessage(responseBody: String, statusCode: Int): String {
        if (responseBody.isBlank()) {
            return "Mobile vendor profile request failed with HTTP $statusCode."
        }

        return try {
            val json = JSONObject(responseBody)
            val code = json.optString("code", "mobile_vendor_profile_error")
            val message = json.optString("message", "Mobile vendor profile request failed.")
            "$code: $message"
        } catch (_: Exception) {
            "Mobile vendor profile request failed with HTTP $statusCode."
        }
    }

    private fun normalizedBaseUrl(): String = baseUrl.trimEnd('/')

    private fun String.encodePathSegment(): String =
        java.net.URLEncoder.encode(this, Charsets.UTF_8.name()).replace("+", "%20")
}
