package app.mobiling.client.data.message.thread

import app.mobiling.client.contract.message.thread.MessageItemPayload
import app.mobiling.client.contract.message.thread.MessageSendRequest
import app.mobiling.client.contract.message.thread.MessageThreadSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class MessageHttpThreadGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) : MessageThreadGateway {
    private val jsonMediaType = "application/json".toMediaType()

    override suspend fun listThreads(): List<MessageThreadSummary> = withContext(Dispatchers.IO) {
        val json = request("GET", "/message/thread", null)
        val items = json.optJSONArray("items")
            ?: json.optJSONObject("payload")?.optJSONArray("items")
            ?: JSONArray()

        buildList {
            for (index in 0 until items.length()) {
                items.optJSONObject(index)?.let { item -> add(threadSummaryFrom(item)) }
            }
        }
    }

    override suspend fun listItems(threadId: String): List<MessageItemPayload> = withContext(Dispatchers.IO) {
        val json = request("GET", "/message/thread/$threadId", null)
        val items = json.optJSONArray("items")
            ?: json.optJSONObject("payload")?.optJSONArray("items")
            ?: JSONArray()

        buildList {
            for (index in 0 until items.length()) {
                items.optJSONObject(index)?.let { item -> add(messageItemFrom(threadId, item)) }
            }
        }
    }

    override suspend fun sendMessage(request: MessageSendRequest): MessageItemPayload = withContext(Dispatchers.IO) {
        val json = request("POST", "/message/thread/${request.threadId}/send", JSONObject().put("body", request.body))
        messageItemFrom(request.threadId, json.optJSONObject("payload") ?: json)
    }

    private fun threadSummaryFrom(item: JSONObject): MessageThreadSummary {
        val threadId = item.optString("threadId", item.optString("thread_id", item.optString("id")))
        val updatedAt = item.optString("updatedAt", item.optString("last_message_at", ""))
        val subject = item.optString("subject", item.optString("title", "")).takeIf(String::isNotBlank)
        val preview = item.optString("lastMessagePreview", item.optString("last_message_id", "No messages yet")).ifBlank { "No messages yet" }

        return MessageThreadSummary(threadId, subject, preview, item.optInt("unreadCount", item.optInt("unread_count", 0)), updatedAt)
    }

    private fun messageItemFrom(threadId: String, item: JSONObject): MessageItemPayload = MessageItemPayload(
        messageId = item.optString("messageId", item.optString("message_id", item.optString("id"))),
        threadId = item.optString("threadId", item.optString("thread_id", threadId)),
        body = item.optString("body", item.optString("text", item.optString("content"))),
        senderId = item.optString("senderId", item.optString("sender_user_id", item.optString("user_id"))),
        sentAtIso8601 = item.optString("sentAt", item.optString("sent_at", item.optString("created_at"))),
    )

    private fun request(method: String, path: String, body: JSONObject?): JSONObject {
        val builder = Request.Builder()
            .url(baseUrl.trimEnd('/') + path)
            .header("Accept", "application/json")
            .header("x-user-id", "1")

        when (method) {
            "POST" -> builder.post((body ?: JSONObject()).toString().toRequestBody(jsonMediaType))
            else -> builder.get()
        }

        client.newCall(builder.build()).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IllegalStateException(errorMessage(responseBody, response.code))
            return JSONObject(responseBody.ifBlank { "{}" })
        }
    }

    private fun errorMessage(responseBody: String, statusCode: Int): String = try {
        JSONObject(responseBody.ifBlank { "{}" }).optString("message", "Messaging request failed with HTTP $statusCode.")
    } catch (_: Exception) {
        "Messaging request failed with HTTP $statusCode."
    }
}

