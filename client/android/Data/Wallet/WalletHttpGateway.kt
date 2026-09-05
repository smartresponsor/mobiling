package app.mobiling.client.data.wallet

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class WalletHttpGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) : WalletGateway {
    override suspend fun loadBalance(): WalletBalancePayload = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/wallet/balance")
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching { JSONObject(responseBody).optString("message") }.getOrNull()
                throw IllegalStateException(message?.takeIf(String::isNotBlank) ?: "Wallet request failed with HTTP ${response.code}.")
            }

            val data = JSONObject(responseBody.ifBlank { "{}" }).optJSONObject("data") ?: JSONObject()
            val currency = data.optJSONArray("currency") ?: JSONArray()
            WalletBalancePayload(
                walletId = data.optString("walletId").takeIf(String::isNotBlank),
                currency = buildList {
                    for (index in 0 until currency.length()) {
                        val item = currency.optJSONObject(index) ?: continue
                        add(WalletCurrencyBalance(
                            code = item.optString("code", "USD"),
                            availableMinor = item.optLong("availableMinor", 0L),
                            reservedMinor = item.optLong("reservedMinor", 0L),
                            totalMinor = item.optLong("totalMinor", 0L),
                        ))
                    }
                },
            )
        }
    }

    override suspend fun loadFunding(): WalletOperationPayload = loadOperation("funding")

    override suspend fun loadWithdrawal(): WalletOperationPayload = loadOperation("withdrawal")

    override suspend fun loadWithdrawal(id: String): WalletOperationItem = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/wallet/withdrawal/$id")
            .header("Accept", "application/json")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw walletError(response.code, responseBody, "Withdrawal detail request failed")
            val data = JSONObject(responseBody.ifBlank { "{}" }).optJSONObject("data") ?: JSONObject()
            operationItem(data, "withdrawal")
        }
    }

    override suspend fun loadWithdrawalDestination(): WalletWithdrawalDestinationPayload = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/wallet/withdrawal/destination")
            .header("Accept", "application/json")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw walletError(response.code, responseBody, "Withdrawal destination request failed")
            val data = JSONObject(responseBody.ifBlank { "{}" }).optJSONObject("data") ?: JSONObject()
            val array = data.optJSONArray("item") ?: JSONArray()
            WalletWithdrawalDestinationPayload(item = buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(WalletWithdrawalDestination(item.optString("id"), item.optString("type"), item.optString("label")))
                }
            })
        }
    }

    override suspend fun requestWithdrawal(amountMinor: Long, currency: String, paymentInstrumentId: String, idempotencyKey: String): WalletOperationItem = withContext(Dispatchers.IO) {
        val payload = JSONObject()
            .put("amountMinor", amountMinor)
            .put("currency", currency)
            .put("paymentInstrumentId", paymentInstrumentId)
            .put("idempotencyKey", idempotencyKey)
        executeWithdrawalWrite("/wallet/withdrawal/request", payload)
    }

    override suspend fun cancelWithdrawal(id: String): WalletOperationItem = withContext(Dispatchers.IO) {
        executeWithdrawalWrite("/wallet/withdrawal/cancel/$id", JSONObject())
    }

    override suspend fun loadTransaction(): WalletTransactionPayload = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/wallet/transaction")
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching { JSONObject(responseBody).optString("message") }.getOrNull()
                throw IllegalStateException(message?.takeIf(String::isNotBlank) ?: "Wallet transaction request failed with HTTP ${response.code}.")
            }

            val data = JSONObject(responseBody.ifBlank { "{}" }).optJSONObject("data") ?: JSONObject()
            val array = data.optJSONArray("item") ?: JSONArray()
            WalletTransactionPayload(
                item = buildList {
                    for (index in 0 until array.length()) {
                        val item = array.optJSONObject(index) ?: continue
                        add(WalletTransactionItem(
                            transactionId = item.optString("transactionId"),
                            type = item.optString("type"),
                            amountMinor = item.optLong("amountMinor", 0L),
                            currency = item.optString("currency", "USD"),
                            postedAt = item.optString("postedAt"),
                        ))
                    }
                },
                nextCursor = data.optString("nextCursor").takeIf(String::isNotBlank),
            )
        }
    }

    private fun executeWithdrawalWrite(path: String, payload: JSONObject): WalletOperationItem {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + path)
            .header("Accept", "application/json")
            .post(payload.toString().toRequestBody(mediaType))
            .build()
        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw walletError(response.code, responseBody, "Withdrawal request failed")
            val data = JSONObject(responseBody.ifBlank { "{}" }).optJSONObject("data") ?: JSONObject()
            return operationItem(data, "withdrawal")
        }
    }

    private fun walletError(code: Int, body: String, fallback: String): IllegalStateException {
        val json = runCatching { JSONObject(body) }.getOrNull()
        val message = json?.optString("message")?.takeIf(String::isNotBlank)
            ?: json?.optString("code")?.takeIf(String::isNotBlank)?.replace('_', ' ')
            ?: "$fallback with HTTP $code."
        return IllegalStateException(message)
    }

    private fun operationItem(item: JSONObject, type: String): WalletOperationItem = WalletOperationItem(
        id = item.optString("id"),
        type = item.optString("type", type),
        status = item.optString("status"),
        amountMinor = item.optLong("amountMinor", 0L),
        currency = item.optString("currency", "USD"),
        transactionId = item.optString("transactionId").takeIf(String::isNotBlank),
        reversalTransactionId = item.optString("reversalTransactionId").takeIf(String::isNotBlank),
        sourceType = item.optString("sourceType").takeIf(String::isNotBlank),
        sourceId = item.optString("sourceId").takeIf(String::isNotBlank),
        sourceReference = item.optString("sourceReference").takeIf(String::isNotBlank),
        destinationReference = item.optString("destinationReference").takeIf(String::isNotBlank),
        railReference = item.optString("railReference").takeIf(String::isNotBlank),
    )

    private suspend fun loadOperation(type: String): WalletOperationPayload = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/wallet/$type")
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching { JSONObject(responseBody).optString("message") }.getOrNull()
                throw IllegalStateException(message?.takeIf(String::isNotBlank) ?: "Wallet $type request failed with HTTP ${response.code}.")
            }
            val data = JSONObject(responseBody.ifBlank { "{}" }).optJSONObject("data") ?: JSONObject()
            val array = data.optJSONArray("item") ?: JSONArray()
            WalletOperationPayload(item = buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(WalletOperationItem(
                        id = item.optString("id"),
                        type = item.optString("type", type),
                        status = item.optString("status"),
                        amountMinor = item.optLong("amountMinor", 0L),
                        currency = item.optString("currency", "USD"),
                        transactionId = item.optString("transactionId").takeIf(String::isNotBlank),
                        reversalTransactionId = item.optString("reversalTransactionId").takeIf(String::isNotBlank),
                        sourceType = item.optString("sourceType").takeIf(String::isNotBlank),
                        sourceId = item.optString("sourceId").takeIf(String::isNotBlank),
                        sourceReference = item.optString("sourceReference").takeIf(String::isNotBlank),
                        destinationReference = item.optString("destinationReference").takeIf(String::isNotBlank),
                        railReference = item.optString("railReference").takeIf(String::isNotBlank),
                    ))
                }
            })
        }
    }
}
