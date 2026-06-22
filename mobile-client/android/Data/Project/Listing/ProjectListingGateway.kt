package app.mobiling.client.data.project.listing

import app.mobiling.client.contract.project.detail.ProjectDetailPayload
import app.mobiling.client.contract.project.listing.ListProjectsQuery
import app.mobiling.client.contract.project.listing.ProjectSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ProjectListingGateway {
    suspend fun listProjects(query: ListProjectsQuery): List<ProjectSummary>

    suspend fun loadProjectDetail(projectId: String): ProjectDetailPayload
}
