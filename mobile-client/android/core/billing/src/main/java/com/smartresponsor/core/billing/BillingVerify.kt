package com.smartresponsor.core.billing

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class BillingVerifier(private val baseUrl: String = "https://httpbin.org") {
    private val client = OkHttpClient()

    fun uploadReceipt(token: String, product: String): Boolean {
        val json = JSONObject(mapOf("token" to token, "product" to product)).toString()
        val body = json.toRequestBody("application/json".toMediaType())
        val req = Request.Builder()
            .url("$baseUrl/anything/mobile/receipt")
            .post(body)
            .build()

        client.newCall(req).execute().use { return it.isSuccessful }
    }

    fun verify(token: String): Boolean {
        val body = ByteArray(0).toRequestBody(null)
        val req = Request.Builder()
            .url("$baseUrl/anything/mobile/verify")
            .post(body)
            .build()

        client.newCall(req).execute().use { return it.isSuccessful }
    }
}
