package app.mobiling.client.data.project

import app.mobiling.client.contract.project.ProjectMobileItemPayload

interface ProjectGateway {
    suspend fun loadProjects(vendorId: String): List<ProjectMobileItemPayload>
    suspend fun createProject(fields: Map<String, String>)
    suspend fun updateProject(projectId: String, fields: Map<String, String>)
    suspend fun deleteProject(projectId: String)
}
