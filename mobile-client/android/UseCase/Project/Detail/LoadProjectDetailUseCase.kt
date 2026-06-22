package app.mobiling.client.client.usecase.project.detail

import app.mobiling.client.client.contract.project.detail.ProjectDetailPayload
import app.mobiling.client.client.data.project.listing.ProjectListingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadProjectDetailUseCase(
    private val projectListingGateway: ProjectListingGateway,
) {
    suspend operator fun invoke(projectId: String): ProjectDetailPayload =
        projectListingGateway.loadProjectDetail(projectId)
}
