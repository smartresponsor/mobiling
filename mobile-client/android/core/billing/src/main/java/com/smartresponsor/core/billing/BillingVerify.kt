package com.smartresponsor.core.billing
import okhttp3.*; import org.json.JSONObject
class BillingVerifier(private val baseUrl:String = "https://httpbin.org") {
  private val client = OkHttpClient()
  fun uploadReceipt(token:String, product:String): Boolean {
    val body = RequestBody.create(MediaType.parse("application/json"), JSONObject(mapOf("token" to token, "product" to product)).toString())
    val req = Request.Builder().url("$baseUrl/anything/mobile/receipt").post(body).build()
    client.newCall(req).execute().use { return it.isSuccessful }
  }
  fun verify(token:String): Boolean {
    val req = Request.Builder().url("$baseUrl/anything/mobile/verify").post(RequestBody.create(null, ByteArray(0))).build()
    client.newCall(req).execute().use { return it.isSuccessful }
  }
}
