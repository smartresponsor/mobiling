package com.smartresponsor.mobile.client.ui.project.listing

import com.smartresponsor.mobile.client.contract.project.listing.projectsummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ProjectListingScreenContract(
    val title: String,
    val rows: List<ProjectSummary>,
    val nextCursor: String?,
)
