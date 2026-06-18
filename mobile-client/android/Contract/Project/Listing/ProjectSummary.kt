package com.smartresponsor.mobile.client.contract.project.listing

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ProjectSummary(
    val projectId: String,
    val projectName: String,
    val stateCode: String,
    val ownerLabel: String,
    val updatedAtIso: String,
)
