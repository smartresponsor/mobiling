package app.mobiling.client.data.retail.placement

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class RetailPlacementHttpGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) : RetailPlacementGateway {
    override suspend fun snapshot(retailId: String) = request("GET", retailId, "placement", null)

    override suspend fun configureFulfillment(retailId: String, fields: Map<String, String>) =
        request("POST", retailId, "fulfillment", fields)

    override suspend fun configureLocation(retailId: String, fields: Map<String, String>) =
        request("POST", retailId, "location", fields)

    override suspend fun configurePricing(retailId: String, fields: Map<String, String>) =
        request("POST", retailId, "pricing", fields)

    override suspend fun publish(retailId: String) = request("POST", retailId, "publish", emptyMap())

    private suspend fun request(
        method: String,
        retailId: String,
        step: String,
        fields: Map<String, String>?,
    ): RetailPlacementSnapshot = withContext(Dispatchers.IO) {
        require(retailId.matches(Regex("^[1-9][0-9]*$"))) { "Retail identity must be a positive integer." }
        val url = baseUrl.trimEnd('/') + "/retail/$retailId/$step"
        val body = fields?.let { JSONObject(it).toString().toRequestBody("application/json".toMediaType()) }
        val builder = Request.Builder().url(url).header("Accept", "application/json")
        if (method == "POST") builder.post(requireNotNull(body)) else builder.get()

        client.newCall(builder.build()).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            val json = JSONObject(raw.ifBlank { "{}" })
            if (!response.isSuccessful) {
                throw IllegalStateException(
                    json.optString("message", "Retail placement request failed with HTTP ${response.code}."),
                )
            }
            json.toSnapshot()
        }
    }

    private fun JSONObject.toSnapshot(): RetailPlacementSnapshot = RetailPlacementSnapshot(
        retailId = opt("retailId")?.toString().orEmpty(),
        kind = optString("kind"),
        catalogCode = optString("catalogCode").takeIf(String::isNotBlank),
        categoryId = opt("categoryId")?.toString()?.takeIf(String::isNotBlank),
        title = optString("title").takeIf(String::isNotBlank),
        status = optString("status").takeIf(String::isNotBlank),
        nextStep = optString("nextStep", "fulfillment"),
        requiresExactLocation = optBoolean("requiresExactLocation"),
        fulfillmentProfile = optJSONObject("fulfillmentProfile")?.toString(),
        locationProfile = optJSONObject("locationProfile")?.toString(),
        pricingProfile = optJSONObject("pricingProfile")?.toString(),
    )
}
