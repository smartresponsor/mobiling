package app.mobiling.client.data.notification

import app.mobiling.client.contract.notification.NotificationInboxItem
import app.mobiling.client.contract.notification.NotificationInboxPayload
import app.mobiling.client.contract.notification.NotificationSubscriptionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class NotificationHttpGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) : NotificationGateway {
    private val jsonMediaType = "application/json".toMediaType()

    override suspend fun inbox(): NotificationInboxPayload = withContext(Dispatchers.IO) {
        val json = request("GET", "/notification", null)
        val items = json.optJSONArray("items")
        val values = buildList {
            if (items != null) {
                for (index in 0 until items.length()) {
                    items.optJSONObject(index)?.let { item ->
                        add(NotificationInboxItem(
                            id = item.optString("id"),
                            notificationId = item.optString("notificationId"),
                            status = item.optString("status", "new"),
                            title = item.optString("title", "Notification"),
                            body = item.optString("body"),
                            priority = item.optString("priority", "normal"),
                            actionUrl = item.optString("actionUrl").takeIf(String::isNotBlank),
                            createdAt = item.optString("createdAt"),
                            readAt = item.optString("readAt").takeIf(String::isNotBlank),
                        ))
                    }
                }
            }
        }
        NotificationInboxPayload(values, json.optInt("unreadCount", 0))
    }

    override suspend fun unreadCount(): Int = withContext(Dispatchers.IO) {
        request("GET", "/notification/unread/count", null).optInt("unreadCount", 0)
    }

    override suspend fun markRead(ids: List<String>): Int = withContext(Dispatchers.IO) {
        val body = JSONObject().put("ids", org.json.JSONArray(ids))
        request("POST", "/notification/mark/read", body).optInt("unreadCount", 0)
    }

    override suspend fun subscription(request: NotificationSubscriptionRequest): Boolean = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("token", request.token)
            .put("platform", request.platform)
            .put("appKey", request.appKey)
            .put("deviceId", request.deviceId)
            .put("enabled", request.enabled)
        request("POST", "/notification/subscription", body).optBoolean("ok", false)
    }

    private fun request(method: String, path: String, body: JSONObject?): JSONObject {
        val builder = Request.Builder()
            .url(baseUrl.trimEnd('/') + path)
            .header("Accept", "application/json")
        if (method == "POST") builder.post((body ?: JSONObject()).toString().toRequestBody(jsonMediaType)) else builder.get()
        client.newCall(builder.build()).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching { JSONObject(responseBody).optString("message") }.getOrNull().orEmpty()
                error(message.ifBlank { "Notification request failed with HTTP ${response.code}." })
            }
            return JSONObject(responseBody.ifBlank { "{}" })
        }
    }
}
