package app.mobiling.client.usecase.project.listing

import app.mobiling.client.contract.project.listing.ProjectListQuery
import app.mobiling.client.contract.project.listing.ProjectSummary
import app.mobiling.client.data.project.listing.ProjectListingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ProjectListUseCase(
    private val projectListingGateway: ProjectListingGateway,
) {
    suspend operator fun invoke(query: ProjectListQuery): List<ProjectSummary> =
        projectListingGateway.listProjects(query)
}
