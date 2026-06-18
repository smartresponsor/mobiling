package com.smartresponsor.mobile.client.usecase.project.listing

import com.smartresponsor.mobile.client.contract.project.listing.listprojectsquery
import com.smartresponsor.mobile.client.contract.project.listing.projectsummary
import com.smartresponsor.mobile.client.data.project.listing.projectlistinggateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class ListProjectsUseCase(
    private val projectListingGateway: ProjectListingGateway,
) {
    suspend operator fun invoke(query: ListProjectsQuery): List<ProjectSummary> =
        projectListingGateway.listProjects(query)
}
