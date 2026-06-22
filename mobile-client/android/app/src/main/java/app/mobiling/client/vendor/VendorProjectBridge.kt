package app.mobiling.client.vendor

import app.mobiling.client.project.ProjectFeatureBridge
import app.mobiling.client.client.contract.project.detail.ProjectDetailPayload
import app.mobiling.client.client.contract.project.listing.ListProjectsQuery
import app.mobiling.client.client.contract.project.listing.ProjectSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 *
 * Vendor-owned project bridge.
 */
class VendorProjectBridge(
    private val feature: ProjectFeatureBridge,
) {
    suspend fun list(query: ListProjectsQuery): List<ProjectSummary> = feature.list(query)

    suspend fun detail(projectId: String): ProjectDetailPayload = feature.detail(projectId)

    fun feature(): ProjectFeatureBridge = feature
}
