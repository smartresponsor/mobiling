package com.smartresponsor.mobile.client.data.system.billing

import com.smartresponsor.mobile.client.contract.system.billing.receiptuploadpayload
import com.smartresponsor.mobile.client.contract.system.billing.receiptverificationresult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class BillingReceiptGateway(private val baseUrl: String = "https://httpbin.org") {
    private val client = OkHttpClient()

    fun upload(payload: ReceiptUploadPayload): Boolean {
        val body = """{"token":"${payload.token}","product":"${payload.product}"}"""
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$baseUrl/anything/mobile/receipt")
            .post(body)
            .build()

        client.newCall(request).execute().use { return it.isSuccessful }
    }

    fun verify(token: String): ReceiptVerificationResult {
        val request = Request.Builder()
            .url("$baseUrl/anything/mobile/verify")
            .post(ByteArray(0).toRequestBody(null))
            .build()

        client.newCall(request).execute().use {
            return ReceiptVerificationResult(accepted = it.isSuccessful)
        }
    }
}
