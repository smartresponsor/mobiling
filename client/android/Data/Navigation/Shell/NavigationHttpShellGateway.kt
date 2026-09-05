package app.mobiling.client.data.navigation.shell

import app.mobiling.client.contract.navigation.shell.NavigationMobileItemPayload
import app.mobiling.client.contract.navigation.shell.NavigationMobileShellPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class NavigationHttpShellGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) : NavigationShellGateway {
    override suspend fun loadMobileShell(): NavigationMobileShellPayload = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(normalizedBaseUrl() + "/navigation/mobile/shell")
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(errorMessage(responseBody, response.code))
            }

            return@withContext payloadFrom(responseBody)
        }
    }

    private fun payloadFrom(responseBody: String): NavigationMobileShellPayload {
        val json = JSONObject(responseBody)
        val locationsJson = json.optJSONObject("locations") ?: JSONObject()
        val locations = mutableMapOf<String, List<NavigationMobileItemPayload>>()

        locationsJson.keys().forEach { location ->
            locations[location] = itemsFrom(locationsJson.optJSONArray(location) ?: JSONArray())
        }

        return NavigationMobileShellPayload(
            schema = json.optString("schema", "smartresponsor.navigation.mobile.shell.v1"),
            channel = json.optString("channel", "mobile"),
            platforms = stringsFrom(json.optJSONArray("platforms") ?: JSONArray()),
            locations = locations,
        )
    }

    private fun itemsFrom(itemsJson: JSONArray): List<NavigationMobileItemPayload> =
        (0 until itemsJson.length()).mapNotNull { index ->
            val item = itemsJson.optJSONObject(index) ?: return@mapNotNull null

            NavigationMobileItemPayload(
                key = item.optString("key"),
                label = item.optString("label"),
                icon = nullableString(item, "icon"),
                badge = nullableString(item, "badge"),
                enabled = item.optBoolean("enabled", false),
                visible = item.optBoolean("visible", true),
                status = item.optString("status", if (item.optBoolean("enabled", false)) "active" else "coming_soon"),
                disabledReason = nullableString(item, "disabledReason"),
                requiredComponent = nullableString(item, "requiredComponent"),
                location = item.optString("location"),
                group = item.optString("group"),
                groupLabel = item.optString("groupLabel"),
                action = nullableString(item, "action"),
                route = nullableString(item, "route"),
            )
        }

    private fun stringsFrom(array: JSONArray): List<String> =
        (0 until array.length()).mapNotNull { index -> nullableArrayString(array, index) }

    private fun nullableString(source: JSONObject, key: String): String? {
        if (source.isNull(key)) {
            return null
        }

        return source.optString(key).takeIf { it.isNotBlank() }
    }

    private fun nullableArrayString(source: JSONArray, index: Int): String? =
        source.optString(index).takeIf { it.isNotBlank() }

    private fun errorMessage(responseBody: String, statusCode: Int): String {
        if (responseBody.isBlank()) {
            return "Mobile navigation shell request failed with HTTP $statusCode."
        }

        return try {
            val json = JSONObject(responseBody)
            val code = json.optString("code", "mobile_navigation_error")
            val message = json.optString("message", "Mobile navigation shell request failed.")
            "$code: $message"
        } catch (_: Exception) {
            "Mobile navigation shell request failed with HTTP $statusCode."
        }
    }

    private fun normalizedBaseUrl(): String = baseUrl.trim().trimEnd('/')
}
