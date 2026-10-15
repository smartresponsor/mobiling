package app.mobiling.client.data.project

import app.mobiling.client.contract.project.ProjectMobileItemPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class ProjectHttpGateway(private val baseUrl: String, private val client: OkHttpClient = OkHttpClient()) : ProjectGateway {
    override suspend fun loadProjects(vendorId: String): List<ProjectMobileItemPayload> {
        val array = request("GET", null, null).optJSONArray("items") ?: JSONArray()
        return buildList {
            for (index in 0 until array.length()) array.optJSONObject(index)?.let { item ->
                val id = item.optString("projectId", item.optString("id"))
                add(ProjectMobileItemPayload(id, item.optString("title", item.optString("name", "Project")), item.optString("status").takeIf(String::isNotBlank), item.optString("location").takeIf(String::isNotBlank)))
            }
        }
    }

    override suspend fun createProject(fields: Map<String, String>) { request("POST", null, fields) }
    override suspend fun updateProject(projectId: String, fields: Map<String, String>) { request("PATCH", projectId, fields) }
    override suspend fun deleteProject(projectId: String) { request("DELETE", projectId, null) }

    private suspend fun request(method: String, identity: String?, fields: Map<String, String>?): JSONObject = withContext(Dispatchers.IO) {
        val url = baseUrl.trimEnd('/') + "/my/project" + (identity?.let { "/$it" } ?: "")
        val payload = fields?.let { JSONObject(it).toString().toRequestBody("application/json".toMediaType()) }
        val builder = Request.Builder().url(url).header("Accept", "application/json")
        when (method) {
            "POST" -> builder.post(requireNotNull(payload))
            "PATCH" -> builder.patch(requireNotNull(payload))
            "DELETE" -> builder.delete()
            else -> builder.get()
        }
        client.newCall(builder.build()).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw IllegalStateException(JSONObject(body.ifBlank { "{}" }).optString("message", "Project CRUD request failed with HTTP ${response.code}."))
            JSONObject(body.ifBlank { "{}" })
        }
    }
}
