package app.mobiling.client.data.auth.session

import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.contract.auth.session.AccessConfirmVerificationRequest
import app.mobiling.client.contract.auth.session.AccessRegisterAuthRequest
import app.mobiling.client.contract.auth.session.AccessRequestRecoveryRequest
import app.mobiling.client.contract.auth.session.AccessResetRecoveryRequest
import app.mobiling.client.contract.auth.session.AccessStartAuthRequest
import app.mobiling.client.contract.auth.session.AccessVerifySecondFactorRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AccessHttpAuthSessionGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) : AccessAuthSessionGateway {
    private val jsonMediaType = "application/json".toMediaType()

    override suspend fun startAuth(request: AccessStartAuthRequest): AccessAuthSessionPayload = sendSessionRequest(
        method = "POST",
        path = "/access/signin",
        body = JSONObject()
            .put("email", request.login)
            .put("password", request.password),
    )

    override suspend fun registerAuth(request: AccessRegisterAuthRequest): AccessAuthSessionPayload = sendSessionRequest(
        method = "POST",
        path = "/access/register",
        body = JSONObject()
            .put("displayName", request.displayName)
            .put("email", request.email)
            .put("password", request.password),
    )

    override suspend fun restoreAuth(): AccessAuthSessionPayload = sendSessionRequest(
        method = "GET",
        path = "/access/session",
        body = null,
    )

    override suspend fun logoutAuth() {
        sendSessionRequest(
            method = "POST",
            path = "/access/logout",
            body = null,
        )
    }

    override suspend fun resendVerification(): AccessAuthSessionPayload = sendSessionRequest(
        method = "POST",
        path = "/access/verification/resend",
        body = null,
    )

    override suspend fun confirmVerification(request: AccessConfirmVerificationRequest): AccessAuthSessionPayload = sendSessionRequest(
        method = "POST",
        path = "/access/verification/confirm",
        body = JSONObject().put("code", request.code),
    )

    override suspend fun challengeSecondFactor(): AccessAuthSessionPayload = sendSessionRequest(
        method = "POST",
        path = "/access/second-factor/challenge",
        body = null,
    )

    override suspend fun verifySecondFactor(request: AccessVerifySecondFactorRequest): AccessAuthSessionPayload = sendSessionRequest(
        method = "POST",
        path = "/access/second-factor/verify",
        body = JSONObject().put("code", request.code),
    )

    override suspend fun requestRecovery(request: AccessRequestRecoveryRequest): AccessAuthSessionPayload = sendSessionRequest(
        method = "POST",
        path = "/access/recovery/request",
        body = JSONObject().put("email", request.email),
    )

    override suspend fun resetRecovery(request: AccessResetRecoveryRequest): AccessAuthSessionPayload = sendSessionRequest(
        method = "POST",
        path = "/access/recovery/reset",
        body = JSONObject()
            .put("email", request.email)
            .put("code", request.code)
            .put("password", request.password),
    )

    private suspend fun sendSessionRequest(method: String, path: String, body: JSONObject?): AccessAuthSessionPayload = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder()
            .url(normalizedBaseUrl() + path)
            .header("Accept", "application/json")

        if (body == null) {
            when (method) {
                "GET" -> requestBuilder.get()
                "POST" -> requestBuilder.post(ByteArray(0).toRequestBody(null))
                else -> throw IllegalArgumentException("Unsupported auth session method: $method")
            }
        } else {
            requestBuilder.post(body.toString().toRequestBody(jsonMediaType))
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(errorMessage(responseBody, response.code))
            }

            return@withContext payloadFrom(responseBody)
        }
    }

    private fun payloadFrom(responseBody: String): AccessAuthSessionPayload {
        if (responseBody.isBlank()) {
            return AccessAuthSessionPayload(
                status = "unauthenticated",
                sessionId = null,
                vendorId = null,
                authenticated = false,
                requiresVerification = false,
                requiresSecondFactor = false,
            )
        }

        val json = JSONObject(responseBody)
        val identity = json.optJSONObject("identity")
        val authenticated = identity != null
        val vendorId = identity?.optString("vendorId")?.trim().takeUnless { it.isNullOrBlank() }

        return AccessAuthSessionPayload(
            status = json.optString("status", if (authenticated) "authenticated" else "unauthenticated"),
            sessionId = null,
            vendorId = vendorId,
            authenticated = authenticated,
            requiresVerification = json.optBoolean("requiresVerification", false),
            requiresSecondFactor = json.optBoolean("requiresSecondFactor", false),
        )
    }

    private fun errorMessage(responseBody: String, statusCode: Int): String {
        if (responseBody.isBlank()) {
            return "Mobile access request failed with HTTP $statusCode."
        }

        return try {
            val json = JSONObject(responseBody)
            val code = json.optString("code", "mobile_access_error")
            val message = json.optString("message", "Mobile access request failed.")
            "$code: $message"
        } catch (_: Exception) {
            "Mobile access request failed with HTTP $statusCode."
        }
    }

    private fun normalizedBaseUrl(): String = baseUrl.trimEnd('/')
}
