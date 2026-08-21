package app.mobiling.client.data.support

import app.mobiling.client.contract.support.CaseRowPayload
import app.mobiling.client.contract.support.SupportActionPayload
import app.mobiling.client.contract.support.SupportFieldPayload
import app.mobiling.client.contract.support.SupportOptionPayload
import app.mobiling.client.contract.support.SupportPagePayload
import app.mobiling.client.contract.support.SupportRowPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

interface SupportGateway {
    suspend fun load(path: String): SupportPagePayload
    suspend fun submit(path: String, fields: Map<String, String>): SupportPagePayload
}

class SupportHttpGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) : SupportGateway {
    private val jsonMediaType = "application/json".toMediaType()

    override suspend fun load(path: String): SupportPagePayload = request("GET", path, null)

    override suspend fun submit(path: String, fields: Map<String, String>): SupportPagePayload =
        request("POST", path, JSONObject(fields))

    private suspend fun request(method: String, path: String, body: JSONObject?): SupportPagePayload = withContext(Dispatchers.IO) {
        require(path.startsWith("/support")) { "Casing support path must stay under /support." }
        val builder = Request.Builder()
            .url(baseUrl.trimEnd('/') + path)
            .header("Accept", "application/json")
        if (method == "POST") {
            builder.post((body ?: JSONObject()).toString().toRequestBody(jsonMediaType))
        } else {
            builder.get()
        }
        client.newCall(builder.build()).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching { JSONObject(responseBody).optString("message") }.getOrNull().orEmpty()
                error(message.ifBlank { "Support request failed with HTTP ${response.code}." })
            }
            parse(JSONObject(responseBody.ifBlank { "{}" }))
        }
    }

    private fun parse(root: JSONObject): SupportPagePayload {
        val data = root.optJSONObject("data") ?: JSONObject()
        val meta = root.optJSONObject("meta") ?: JSONObject()
        val content = root.optJSONObject("interface")
            ?.optJSONObject("locations")
            ?.optJSONArray("shell.main.content")
            ?.optJSONObject(0)
        val rows = data.optJSONArray("rows") ?: JSONArray()
        val supportRows = mutableListOf<SupportRowPayload>()
        val caseRows = mutableListOf<CaseRowPayload>()
        for (index in 0 until rows.length()) {
            val row = rows.optJSONObject(index) ?: continue
            if (row.has("availableItems") || row.has("request")) {
                supportRows += SupportRowPayload(
                    id = row.optString("id"),
                    context = row.optString("context"),
                    request = row.optString("request"),
                    description = row.optString("description"),
                    href = row.optString("href"),
                    availableItems = row.optInt("availableItems", 0),
                )
            } else if (row.has("reference")) {
                caseRows += CaseRowPayload(
                    reference = row.optString("reference"),
                    context = row.optString("context"),
                    category = row.optString("category"),
                    status = row.optString("status"),
                    href = row.optString("href"),
                )
            }
        }

        return SupportPagePayload(
            title = meta.optString("title", content?.optString("label", "Support") ?: "Support"),
            description = content?.optString("description").orEmpty(),
            rows = supportRows,
            cases = caseRows,
            fields = parseFields(data.optJSONArray("formFields") ?: data.optJSONObject("informationForm")?.optJSONArray("fields")),
            actions = parseActions(data.optJSONArray("headerActions") ?: data.optJSONArray("actions")),
            action = data.optString("action").takeIf(String::isNotBlank)
                ?: data.optJSONObject("informationForm")?.optString("action")?.takeIf(String::isNotBlank),
            method = data.optString("method", data.optJSONObject("informationForm")?.optString("method", "GET") ?: "GET"),
            reference = data.optString("reference").takeIf(String::isNotBlank)
                ?: data.optString("caseReference").takeIf(String::isNotBlank),
            status = data.optString("status").takeIf(String::isNotBlank),
            businessContext = data.optString("businessContext").takeIf(String::isNotBlank),
            category = data.optString("category").takeIf(String::isNotBlank),
            descriptionText = data.optString("description").takeIf(String::isNotBlank),
            informationQuestion = data.optJSONObject("informationRequest")?.optString("question")?.takeIf(String::isNotBlank),
        )
    }

    private fun parseFields(array: JSONArray?): List<SupportFieldPayload> = buildList {
        if (array == null) return@buildList
        for (index in 0 until array.length()) {
            val field = array.optJSONObject(index) ?: continue
            val options = field.optJSONArray("options") ?: JSONArray()
            add(SupportFieldPayload(
                name = field.optString("nameEntity"),
                label = field.optString("label"),
                type = field.optString("type", "text"),
                value = field.opt("value")?.toString()?.takeIf { it != "null" },
                required = field.optBoolean("required", false),
                options = buildList {
                    for (optionIndex in 0 until options.length()) {
                        val option = options.optJSONObject(optionIndex) ?: continue
                        add(SupportOptionPayload(option.optString("label"), option.optString("value")))
                    }
                },
            ))
        }
    }

    private fun parseActions(array: JSONArray?): List<SupportActionPayload> = buildList {
        if (array == null) return@buildList
        for (index in 0 until array.length()) {
            val action = array.optJSONObject(index) ?: continue
            add(SupportActionPayload(
                label = action.optString("label"),
                href = action.optString("href"),
                method = action.optString("method", "GET"),
                enabled = action.optBoolean("enabled", true),
            ))
        }
    }
}
