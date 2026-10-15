package app.mobiling.client.data.currency

import app.mobiling.client.contract.currency.CurrencyCatalogPayload
import app.mobiling.client.contract.currency.CurrencyChoicePayload
import app.mobiling.client.contract.currency.CurrencyMetadataPayload
import app.mobiling.client.contract.currency.CurrencySelectorPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

interface CurrencyGateway {
    suspend fun loadCatalog(locale: String? = null): CurrencyCatalogPayload
    suspend fun loadSelector(selectedCode: String? = null, locale: String? = null): CurrencySelectorPayload
}

class CurrencyHttpGateway(
    private val baseUrl: String,
    private val client: OkHttpClient = OkHttpClient(),
) : CurrencyGateway {
    override suspend fun loadCatalog(locale: String?): CurrencyCatalogPayload = withContext(Dispatchers.IO) {
        val json = get("/currency/catalog" + query(mapOf("locale" to locale)))
        val array = json.optJSONArray("items")
        CurrencyCatalogPayload((0 until (array?.length() ?: 0)).mapNotNull { index ->
            array?.optJSONObject(index)?.let { item ->
                CurrencyMetadataPayload(
                    code = item.optString("code", ""),
                    numericCode = stringOrNull(item, "numericCode"),
                    minorUnit = item.optInt("minorUnit", 2),
                    symbol = stringOrNull(item, "symbol"),
                    displayName = stringOrNull(item, "displayName"),
                )
            }
        })
    }

    override suspend fun loadSelector(selectedCode: String?, locale: String?): CurrencySelectorPayload = withContext(Dispatchers.IO) {
        val json = get("/currency/selector" + query(mapOf("selectedCode" to selectedCode, "locale" to locale)))
        val array = json.optJSONArray("choices")
        val choices = (0 until (array?.length() ?: 0)).mapNotNull { index ->
            array?.optJSONObject(index)?.let { item ->
                CurrencyChoicePayload(
                    code = item.optString("code", ""),
                    label = item.optString("label", item.optString("code", "")),
                    symbol = stringOrNull(item, "symbol"),
                    minorUnit = item.optInt("minorUnit", 2),
                )
            }
        }
        CurrencySelectorPayload(
            choices = choices,
            selectedCode = stringOrNull(json, "selectedCode"),
            placeholder = json.optString("placeholder", "currency.form.placeholder"),
        )
    }

    private fun get(path: String): JSONObject {
        val request = Request.Builder().url(baseUrl.trimEnd('/') + path).header("Accept", "application/json").get().build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IllegalStateException("Mobile currency request failed with HTTP ${response.code}.")
            return if (body.isBlank()) JSONObject() else JSONObject(body)
        }
    }

    private fun query(values: Map<String, String?>): String {
        val parts = values.mapNotNull { (key, value) -> value?.trim()?.takeIf(String::isNotEmpty)?.let {
            URLEncoder.encode(key, StandardCharsets.UTF_8) + "=" + URLEncoder.encode(it, StandardCharsets.UTF_8)
        } }
        return if (parts.isEmpty()) "" else "?" + parts.joinToString("&")
    }

    private fun stringOrNull(json: JSONObject, key: String): String? =
        json.optString(key).trim().takeUnless { it.isBlank() || "null" == it }
}

