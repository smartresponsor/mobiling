package com.smartresponsor.mobile.app.vendor

import com.smartresponsor.mobile.app.project.ProjectFeatureBridge
import com.smartresponsor.mobile.client.contract.project.detail.ProjectDetailPayload
import com.smartresponsor.mobile.client.contract.project.listing.ListProjectsQuery
import com.smartresponsor.mobile.client.contract.project.listing.ProjectSummary

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
