package app.mobiling.client.data.system.push

import app.mobiling.client.contract.system.push.PushRegistrationPayload
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class PushRegistrationGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) {

    fun register(payload: PushRegistrationPayload): Boolean {
        val body = JSONObject()
            .put("token", payload.token)
            .put("platform", payload.platform)
            .put("appKey", payload.appKey)
            .put("deviceId", payload.deviceId)
            .put("enabled", payload.enabled)
            .toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/notification/subscription")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            return response.isSuccessful
        }
    }
}
