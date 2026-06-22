package app.mobiling.client.ui.project.listing

import app.mobiling.client.contract.project.listing.ProjectSummary

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ProjectListingScreenContract(
    val title: String,
    val rows: List<ProjectSummary>,
    val nextCursor: String?,
)
