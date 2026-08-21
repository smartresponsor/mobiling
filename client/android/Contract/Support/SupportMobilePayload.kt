package app.mobiling.client.contract.support

data class SupportOptionPayload(val label: String, val value: String)

data class SupportFieldPayload(
    val name: String,
    val label: String,
    val type: String,
    val value: String?,
    val required: Boolean,
    val options: List<SupportOptionPayload>,
)

data class SupportActionPayload(
    val label: String,
    val href: String,
    val method: String,
    val enabled: Boolean,
)

data class SupportRowPayload(
    val id: String,
    val context: String,
    val request: String,
    val description: String,
    val href: String,
    val availableItems: Int,
)

data class CaseRowPayload(
    val reference: String,
    val context: String,
    val category: String,
    val status: String,
    val href: String,
)

data class SupportPagePayload(
    val title: String,
    val description: String,
    val rows: List<SupportRowPayload> = emptyList(),
    val cases: List<CaseRowPayload> = emptyList(),
    val fields: List<SupportFieldPayload> = emptyList(),
    val actions: List<SupportActionPayload> = emptyList(),
    val action: String? = null,
    val method: String = "GET",
    val reference: String? = null,
    val status: String? = null,
    val businessContext: String? = null,
    val category: String? = null,
    val descriptionText: String? = null,
)
