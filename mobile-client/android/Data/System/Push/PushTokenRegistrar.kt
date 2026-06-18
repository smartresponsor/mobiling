package com.smartresponsor.mobile.client.data.system.push

import com.smartresponsor.mobile.client.contract.system.push.PushRegistrationPayload
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class PushTokenRegistrar(private val baseUrl: String = "https://httpbin.org") {
    private val client = OkHttpClient()

    fun register(payload: PushRegistrationPayload): Boolean {
        val body = payload.token.toRequestBody("text/plain".toMediaType())
        val request = Request.Builder()
            .url("$baseUrl/anything/mobile/push/token?platform=${payload.platform}")
            .post(body)
            .build()

        client.newCall(request).execute().use { return it.isSuccessful }
    }
}
