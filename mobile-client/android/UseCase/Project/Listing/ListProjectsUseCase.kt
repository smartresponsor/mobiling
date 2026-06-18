package com.smartresponsor.mobile.client.usecase.project.listing

import com.smartresponsor.mobile.client.contract.project.listing.ListProjectsQuery
import com.smartresponsor.mobile.client.contract.project.listing.ProjectSummary
import com.smartresponsor.mobile.client.data.project.listing.ProjectListingGateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListProjectsUseCase(
    private val projectListingGateway: ProjectListingGateway,
) {
    suspend operator fun invoke(query: ListProjectsQuery): List<ProjectSummary> =
        projectListingGateway.listProjects(query)
}
