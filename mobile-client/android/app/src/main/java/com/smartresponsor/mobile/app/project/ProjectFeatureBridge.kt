package com.smartresponsor.mobile.app.project

import com.smartresponsor.mobile.client.contract.project.detail.ProjectDetailPayload
import com.smartresponsor.mobile.client.contract.project.listing.ListProjectsQuery
import com.smartresponsor.mobile.client.contract.project.listing.ProjectSummary
import com.smartresponsor.mobile.client.data.project.listing.ProjectListingGateway
import com.smartresponsor.mobile.client.usecase.project.detail.LoadProjectDetailUseCase
import com.smartresponsor.mobile.client.usecase.project.listing.ListProjectsUseCase

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
