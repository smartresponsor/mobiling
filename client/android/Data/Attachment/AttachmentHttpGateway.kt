package app.mobiling.client.data.attachment

import app.mobiling.client.contract.attachment.AttachmentItemPayload
import app.mobiling.client.contract.attachment.AttachmentLinkPayload
import app.mobiling.client.contract.attachment.AttachmentLinkRequest
import app.mobiling.client.contract.attachment.AttachmentListPayload
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class AttachmentHttpGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) : AttachmentReader, AttachmentWriter {
    private val jsonMediaType = "application/json".toMediaType()

    override suspend fun listAttachment(
        ownerType: String,
        ownerId: String,
        context: String?,
        slot: String?,
    ): AttachmentListPayload {
        val query = buildList {
            add("ownerType=${ownerType.encodeQuery()}")
            add("ownerId=${ownerId.encodeQuery()}")
            context?.takeIf { it.isNotBlank() }?.let { add("context=${it.encodeQuery()}") }
            slot?.takeIf { it.isNotBlank() }?.let { add("slot=${it.encodeQuery()}") }
        }.joinToString("&")

        return listFrom(sendAttachmentRequest(method = "GET", path = "/attachment?$query", body = null))
    }

    override suspend fun attachAttachment(request: AttachmentLinkRequest): AttachmentLinkPayload {
        val body = JSONObject()
            .put("attachmentId", request.attachmentId)
            .put("ownerType", request.ownerType)
            .put("ownerId", request.ownerId)
            .put("position", request.position)
            .put("isPrimary", request.isPrimary)

        request.context?.takeIf { it.isNotBlank() }?.let { body.put("context", it) }
        request.slot?.takeIf { it.isNotBlank() }?.let { body.put("slot", it) }

        return linkFrom(sendAttachmentRequest(method = "POST", path = "/attachment/link", body = body))
    }

    private fun listFrom(json: JSONObject): AttachmentListPayload {
        val array = json.optJSONArray("items")
        val items = (0 until (array?.length() ?: 0)).mapNotNull { index ->
            array?.optJSONObject(index)?.let(::itemFrom)
        }

        return AttachmentListPayload(
            ownerType = json.optString("ownerType", ""),
            ownerId = json.optString("ownerId", ""),
            count = json.optInt("count", items.size),
            items = items,
            payloadText = json.optString("payloadText", ""),
        )
    }

    private fun itemFrom(json: JSONObject): AttachmentItemPayload = AttachmentItemPayload(
        attachmentId = json.optString("attachmentId", ""),
        type = json.optString("type", "attachment"),
        mimeType = stringOrNull(json, "mimeType"),
        downloadUrl = stringOrNull(json, "downloadUrl"),
        payloadText = json.optString("payloadText", ""),
    )

    private fun linkFrom(json: JSONObject): AttachmentLinkPayload = AttachmentLinkPayload(
        linkId = json.optString("linkId", ""),
        attachmentId = json.optString("attachmentId", ""),
        ownerType = json.optString("ownerType", ""),
        ownerId = json.optString("ownerId", ""),
        context = stringOrNull(json, "context"),
        slot = stringOrNull(json, "slot"),
        position = json.optInt("position", 0),
        isPrimary = json.optBoolean("isPrimary", false),
        payloadText = json.optString("payloadText", ""),
    )

    private fun sendAttachmentRequest(method: String, path: String, body: JSONObject?): JSONObject {
        val requestBuilder = Request.Builder()
            .url(normalizedBaseUrl() + path)
            .header("Accept", "application/json")

        when {
            method == "GET" -> requestBuilder.get()
            method == "POST" && body == null -> requestBuilder.post(ByteArray(0).toRequestBody(null))
            method == "POST" -> requestBuilder.post(body.toString().toRequestBody(jsonMediaType))
            else -> throw IllegalArgumentException("Unsupported attachment method: $method")
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(errorMessage(responseBody, response.code))
            }

            return if (responseBody.isBlank()) JSONObject() else JSONObject(responseBody)
        }
    }

    private fun errorMessage(responseBody: String, statusCode: Int): String {
        if (responseBody.isBlank()) {
            return "Mobile attachment request failed with HTTP $statusCode."
        }

        return try {
            val json = JSONObject(responseBody)
            val code = json.optString("code", "mobile_attachment_error")
            val message = json.optString("message", "Mobile attachment request failed.")
            "$code: $message"
        } catch (_: Exception) {
            "Mobile attachment request failed with HTTP $statusCode."
        }
    }

    private fun stringOrNull(json: JSONObject, key: String): String? =
        json.optString(key).trim().takeUnless { it.isBlank() || it == "null" }

    private fun normalizedBaseUrl(): String = baseUrl.trimEnd('/')

    private fun String.encodeQuery(): String =
        java.net.URLEncoder.encode(this, Charsets.UTF_8.name()).replace("+", "%20")
}
