package app.mobiling.client.contract.project.listing

/**
 * Marketing America Corp. Oleksandr Tishchenko
 */
data class ProjectListQuery(
    val searchTerm: String?,
    val stateCode: String?,
    val cursor: String?,
    val limit: Int,
)
