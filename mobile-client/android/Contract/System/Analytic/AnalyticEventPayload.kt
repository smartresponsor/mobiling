package app.mobiling.client.contract.system.analytic

/**
 * Canonical contract payload for analytics event emission.
 */
data class AnalyticEventPayload(
    val name: String,
    val attributes: Map<String, String> = emptyMap(),
    val occurredAtIso8601: String? = null,
)
