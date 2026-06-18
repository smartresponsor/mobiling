package com.smartresponsor.mobile.client.data.project.listing

import com.smartresponsor.mobile.client.contract.project.detail.ProjectDetailPayload
import com.smartresponsor.mobile.client.contract.project.listing.ListProjectsQuery
import com.smartresponsor.mobile.client.contract.project.listing.ProjectSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
interface ProjectListingGateway {
    suspend fun listProjects(query: ListProjectsQuery): List<ProjectSummary>

    suspend fun loadProjectDetail(projectId: String): ProjectDetailPayload
}
