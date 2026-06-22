package app.mobiling.client.project

import app.mobiling.client.contract.project.detail.ProjectDetailPayload
import app.mobiling.client.contract.project.listing.ListProjectsQuery
import app.mobiling.client.contract.project.listing.ProjectSummary
import app.mobiling.client.data.project.listing.ProjectListingGateway
import app.mobiling.client.usecase.project.detail.LoadProjectDetailUseCase
import app.mobiling.client.usecase.project.listing.ListProjectsUseCase

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * App-level bridge for project-domain controlled rewire.
 */
@Deprecated(
    message = "Prefer VendorBusinessBridge.ownedProject() or VendorNavigationBridge.project() for vendor-owned navigation.",
    replaceWith = ReplaceWith("vendorBusinessBridge.ownedProject()")
)
class ProjectFeatureBridge(
    private val listingGateway: ProjectListingGateway,
) {
    suspend fun list(query: ListProjectsQuery): List<ProjectSummary> =
        ListProjectsUseCase(listingGateway).invoke(query)

    suspend fun detail(projectId: String): ProjectDetailPayload =
        LoadProjectDetailUseCase(listingGateway).invoke(projectId)
}
