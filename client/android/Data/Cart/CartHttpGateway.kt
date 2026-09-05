package app.mobiling.client.data.cart

import app.mobiling.client.contract.cart.CartAddItemRequest
import app.mobiling.client.contract.cart.CartCheckoutHandoffPayload
import app.mobiling.client.contract.cart.CartItemPayload
import app.mobiling.client.contract.cart.CartMobilePayload
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class CartHttpGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
    private val tokenStore: CartTokenStore = InMemoryCartTokenStore(),
) : CartReader, CartWriter, CartCheckoutGateway {
    private val jsonMediaType = "application/json".toMediaType()

    override suspend fun currentCart(): CartMobilePayload = cartFrom(
        sendCartRequest(method = "GET", path = "/cart/current", body = null),
    )

    override suspend fun addItem(request: CartAddItemRequest): CartMobilePayload {
        val body = JSONObject()
            .put("offerReference", request.offerReference)
            .put("quantity", request.quantity)

        request.title?.let { body.put("title", it) }
        request.unitPriceMinor?.let { body.put("unitPriceMinor", it) }
        request.currencyCode?.let { body.put("currencyCode", it) }

        return cartFrom(sendCartRequest(method = "POST", path = "/cart/item", body = body))
    }

    override suspend fun prepareCheckoutHandoff(): CartCheckoutHandoffPayload = handoffFrom(
        sendCartRequest(method = "POST", path = "/cart/checkout-handoff", body = null),
    )

    private fun cartFrom(json: JSONObject): CartMobilePayload {
        val array = json.optJSONArray("items")
        val items = (0 until (array?.length() ?: 0)).mapNotNull { index ->
            array?.optJSONObject(index)?.let(::itemFrom)
        }

        return CartMobilePayload(
            cartId = stringOrNull(json, "cartId"),
            cartToken = json.optString("cartToken", ""),
            ownerReference = stringOrNull(json, "ownerReference"),
            status = json.optString("status", "active"),
            currencyCode = json.optString("currencyCode", "USD"),
            itemCount = json.optInt("itemCount", items.size),
            subtotalMinor = json.optLong("subtotalMinor", 0L),
            totalMinor = json.optLong("totalMinor", 0L),
            items = items,
            expiresAt = stringOrNull(json, "expiresAt"),
            updatedAt = stringOrNull(json, "updatedAt"),
        )
    }

    private fun itemFrom(json: JSONObject): CartItemPayload = CartItemPayload(
        itemId = json.optString("itemId", ""),
        offerReference = json.optString("offerReference", ""),
        title = json.optString("title", "Cart item"),
        unitPriceMinor = json.optLong("unitPriceMinor", 0L),
        currencyCode = json.optString("currencyCode", "USD"),
        quantity = json.optInt("quantity", 1),
        lineTotalMinor = json.optLong("lineTotalMinor", 0L),
    )

    private fun handoffFrom(json: JSONObject): CartCheckoutHandoffPayload = CartCheckoutHandoffPayload(
        cartId = stringOrNull(json, "cartId"),
        cartToken = json.optString("cartToken", ""),
        handoffId = json.optString("handoffId", ""),
        checkoutUrl = stringOrNull(json, "checkoutUrl"),
        status = json.optString("status", "prepared"),
        expiresAt = stringOrNull(json, "expiresAt"),
    )

    private fun stringOrNull(json: JSONObject, key: String): String? =
        json.optString(key).trim().takeUnless { it.isBlank() || it == "null" }

    private fun sendCartRequest(method: String, path: String, body: JSONObject?): JSONObject {
        val requestBuilder = Request.Builder()
            .url(normalizedBaseUrl() + path)
            .header("Accept", "application/json")
        tokenStore.current()?.let { requestBuilder.header("X-Cart-Token", it) }

        if (body == null) {
            when (method) {
                "GET" -> requestBuilder.get()
                "POST" -> requestBuilder.post(ByteArray(0).toRequestBody(null))
                else -> throw IllegalArgumentException("Unsupported cart method: $method")
            }
        } else {
            requestBuilder.post(body.toString().toRequestBody(jsonMediaType))
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            response.header("X-Cart-Token")?.let(tokenStore::save)
            if (!response.isSuccessful) {
                throw IllegalStateException("Mobile cart request failed with HTTP ${response.code}.")
            }

            val json = if (responseBody.isBlank()) JSONObject() else JSONObject(responseBody)
            json.optString("cartToken").trim().takeIf(String::isNotEmpty)?.let(tokenStore::save)
            return json
        }
    }

    private fun normalizedBaseUrl(): String = baseUrl.trimEnd('/')
}
