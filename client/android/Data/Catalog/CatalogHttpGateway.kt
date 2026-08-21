package app.mobiling.client.data.catalog

import app.mobiling.client.contract.catalog.browse.CatalogListNodeQuery
import app.mobiling.client.contract.catalog.browse.CatalogNodeSummary
import app.mobiling.client.contract.catalog.detail.CatalogNodeDetailPayload
import app.mobiling.client.data.catalog.browse.CatalogBrowseGateway
import app.mobiling.client.data.catalog.detail.CatalogNodeDetailGateway
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class CatalogHttpGateway(
    private val baseUrl: String,
    private val catalogCode: String,
    private val client: OkHttpClient = OkHttpClient(),
) : CatalogBrowseGateway, CatalogNodeDetailGateway {
    override suspend fun listNodes(query: CatalogListNodeQuery): List<CatalogNodeSummary> = withContext(Dispatchers.IO) {
        val url = (baseUrl.trimEnd('/') + "/catalog").toHttpUrl().newBuilder()
            .addQueryParameter("catalogCode", query.catalogCode?.takeIf(String::isNotBlank) ?: catalogCode)
            .apply {
                query.parentNodeId?.takeIf(String::isNotBlank)?.let { addQueryParameter("parentNodeId", it) }
                query.searchText?.takeIf(String::isNotBlank)?.let { addQueryParameter("q", it) }
            }
            .build()
        val root = get(url.toString())
        parseNodes(root.optJSONArray("nodes") ?: JSONArray())
    }

    override suspend fun loadNodeDetail(nodeId: String): CatalogNodeDetailPayload = withContext(Dispatchers.IO) {
        val root = get(baseUrl.trimEnd('/') + "/catalog/node/" + java.net.URLEncoder.encode(nodeId, Charsets.UTF_8.name()))
        val node = parseNode(root.optJSONObject("node") ?: JSONObject())
        CatalogNodeDetailPayload(
            node = node,
            description = root.optString("description").takeIf(String::isNotBlank),
            breadcrumbLabels = root.optJSONArray("breadcrumbLabels").strings(),
            featuredProductIds = root.optJSONArray("featuredProductIds").strings(),
        )
    }

    private fun get(url: String): JSONObject {
        client.newCall(Request.Builder().url(url).header("Accept", "application/json").get().build()).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching { JSONObject(body).optString("message") }.getOrNull()
                throw IllegalStateException(message?.takeIf(String::isNotBlank) ?: "Catalog request failed with HTTP ${response.code}.")
            }
            return JSONObject(body.ifBlank { "{}" })
        }
    }

    private fun parseNodes(array: JSONArray): List<CatalogNodeSummary> = buildList {
        for (index in 0 until array.length()) array.optJSONObject(index)?.let { add(parseNode(it)) }
    }

    private fun parseNode(item: JSONObject) = CatalogNodeSummary(
        nodeId = item.optString("nodeId"), parentNodeId = item.optString("parentNodeId").takeIf(String::isNotBlank),
        title = item.optString("title", "Catalog item"), slug = item.optString("slug").takeIf(String::isNotBlank),
        imageUrl = item.optString("imageUrl").takeIf(String::isNotBlank),
        childCount = item.optInt("childCount", 0), productCount = item.optInt("productCount", 0),
    )
}

private fun JSONArray?.strings(): List<String> = if (this == null) {
    emptyList()
} else {
    buildList {
        for (index in 0 until length()) {
            optString(index).takeIf(String::isNotBlank)?.let(::add)
        }
    }
}
