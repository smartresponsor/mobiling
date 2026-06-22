package app.mobiling.client.client.contract.project.detail

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ProjectDetailPayload(
    val projectId: String,
    val projectName: String,
    val stateCode: String,
    val ownerLabel: String,
    val summary: String,
    val updatedAtIso: String,
)
