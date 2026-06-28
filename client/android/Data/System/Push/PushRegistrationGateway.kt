package app.mobiling.client.data.system.push

import app.mobiling.client.contract.system.push.PushRegistrationPayload
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class PushRegistrationGateway(private val baseUrl: String = "https://httpbin.org") {
    private val client = OkHttpClient()

    fun register(payload: PushRegistrationPayload): Boolean {
        val body = """{"token":"${payload.token}","platform":"${payload.platform}"}"""
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$baseUrl/anything/mobile/push/register")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            return response.isSuccessful
        }
    }
}
