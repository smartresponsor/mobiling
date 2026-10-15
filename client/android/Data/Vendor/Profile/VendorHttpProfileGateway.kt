package app.mobiling.client.data.vendor.profile

import app.mobiling.client.contract.vendor.profile.VendorMobileProfilePayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URI

class VendorHttpProfileGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) : VendorProfileGateway {
    private val cache = java.util.concurrent.ConcurrentHashMap<String, VendorMobileProfilePayload>()

    override suspend fun loadVendorProfile(vendorId: String): VendorMobileProfilePayload {
        var lastFailure: Exception? = null

        repeat(2) { attempt ->
            try {
                val payload = loadRemote(vendorId)
                cache[vendorId] = payload
                return payload
            } catch (exception: Exception) {
                lastFailure = exception
                if (attempt == 0) {
                    delay(250)
                }
            }
        }

        return cache[vendorId] ?: throw (lastFailure ?: IllegalStateException("Vendor profile is temporarily unavailable."))
    }

    private suspend fun loadRemote(vendorId: String): VendorMobileProfilePayload = withContext(Dispatchers.IO) {
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

            payloadFrom(vendorId, responseBody)
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
            avatarUrl = normalizeMediaUrl(nullableString(json, "avatarUrl")),
            avatarAttachmentId = nullableString(json, "avatarAttachmentId"),
            coverUrl = normalizeMediaUrl(nullableString(json, "coverUrl")),
            coverAttachmentId = nullableString(json, "coverAttachmentId"),
            canEditProfileMedia = json.optBoolean("canEditProfileMedia", false),
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

    private fun normalizeMediaUrl(value: String?): String? {
        if (value.isNullOrBlank()) {
            return null
        }

        return try {
            val mediaUri = URI(value)
            val apiUri = URI(normalizedBaseUrl())
            val mediaHost = mediaUri.host?.lowercase()
            val apiHost = apiUri.host

            if (mediaUri.isAbsolute && mediaHost in setOf("127.0.0.1", "localhost") && !apiHost.isNullOrBlank() && apiHost.lowercase() !in setOf("127.0.0.1", "localhost")) {
                URI(mediaUri.scheme, mediaUri.userInfo, apiHost, mediaUri.port, mediaUri.path, mediaUri.query, mediaUri.fragment).toString()
            } else {
                value
            }
        } catch (_: Exception) {
            value
        }
    }

    private fun normalizedBaseUrl(): String = baseUrl.trimEnd('/')

    private fun String.encodePathSegment(): String =
        java.net.URLEncoder.encode(this, Charsets.UTF_8.name()).replace("+", "%20")
}
