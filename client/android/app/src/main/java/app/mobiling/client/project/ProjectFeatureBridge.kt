package app.mobiling.client.project

import app.mobiling.client.contract.project.detail.ProjectDetailPayload
import app.mobiling.client.contract.project.listing.ProjectListQuery
import app.mobiling.client.contract.project.listing.ProjectSummary
import app.mobiling.client.data.project.listing.ProjectListingGateway
import app.mobiling.client.usecase.project.detail.ProjectLoadDetailUseCase
import app.mobiling.client.usecase.project.listing.ProjectListUseCase

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
    suspend fun list(query: ProjectListQuery): List<ProjectSummary> =
        ProjectListUseCase(listingGateway).invoke(query)

    suspend fun detail(projectId: String): ProjectDetailPayload =
        ProjectLoadDetailUseCase(listingGateway).invoke(projectId)
}
