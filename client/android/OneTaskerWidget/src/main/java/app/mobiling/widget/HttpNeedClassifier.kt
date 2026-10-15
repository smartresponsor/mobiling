package app.mobiling.widget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class HttpNeedClassifier(
    private val baseUrl: String,
    private val client: OkHttpClient,
) : NeedClassifier {
    private val jsonMediaType = "application/json".toMediaType()

    override suspend fun classify(transcript: String, localeTag: String?): RecognizedNeed = withContext(Dispatchers.IO) {
        val boundedTranscript = transcript.trim().take(1200)
        require(boundedTranscript.isNotBlank()) { "Transcript is required." }
        val body = JSONObject().put("transcript", boundedTranscript)
        localeTag?.trim()?.takeIf(String::isNotBlank)?.let { body.put("locale", it.take(32)) }
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/need/classify")
            .header("Accept", "application/json")
            .post(body.toString().toRequestBody(jsonMediaType))
            .build()
        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching { JSONObject(responseBody).optString("message") }.getOrNull().orEmpty()
                error(message.ifBlank { "Need classification failed with HTTP ${response.code}." })
            }
            val json = JSONObject(responseBody.ifBlank { "{}" })
            RecognizedNeed(
                isNeed = json.optBoolean("isNeed", false),
                category = json.optString("category").takeIf(String::isNotBlank),
                summary = json.optString("summary").takeIf(String::isNotBlank),
                confidence = json.optDouble("confidence", 0.0).coerceIn(0.0, 1.0),
                classifierVersion = json.optString("classifierVersion", "unknown"),
            )
        }
    }
}
