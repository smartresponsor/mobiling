package com.smartresponsor.mobile.client.usecase.project.detail

import com.smartresponsor.mobile.client.contract.project.detail.projectdetailpayload
import com.smartresponsor.mobile.client.data.project.listing.projectlistinggateway

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
class LoadProjectDetailUseCase(
    private val projectListingGateway: ProjectListingGateway,
) {
    suspend operator fun invoke(projectId: String): ProjectDetailPayload =
        projectListingGateway.loadProjectDetail(projectId)
}
