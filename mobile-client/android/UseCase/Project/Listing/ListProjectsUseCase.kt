package app.mobiling.client.client.usecase.project.listing

import app.mobiling.client.client.contract.project.listing.ListProjectsQuery
import app.mobiling.client.client.contract.project.listing.ProjectSummary
import app.mobiling.client.client.data.project.listing.ProjectListingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListProjectsUseCase(
    private val projectListingGateway: ProjectListingGateway,
) {
    suspend operator fun invoke(query: ListProjectsQuery): List<ProjectSummary> =
        projectListingGateway.listProjects(query)
}
