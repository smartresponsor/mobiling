package com.smartresponsor.mobile.client.data.project.listing

import com.smartresponsor.mobile.client.contract.project.detail.projectdetailpayload
import com.smartresponsor.mobile.client.contract.project.listing.listprojectsquery
import com.smartresponsor.mobile.client.contract.project.listing.projectsummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ProjectListingGateway {
    suspend fun listProjects(query: ListProjectsQuery): List<ProjectSummary>

    suspend fun loadProjectDetail(projectId: String): ProjectDetailPayload
}
