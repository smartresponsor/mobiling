package app.mobiling.core.analytic

import app.mobiling.client.client.contract.system.analytic.AnalyticEventPayload
import app.mobiling.client.client.data.system.analytic.AnalyticEventRecorder
import app.mobiling.client.client.usecase.system.analytic.RecordAnalyticEventUseCase

/**
 * Legacy-compatible Android entry point bridged to canonical system/analytic slices.
 */
class Analytic(
    private val recorder: AnalyticEventRecorder = AnalyticEventRecorder(),
) {
    private val recordAnalyticEventUseCase: RecordAnalyticEventUseCase = RecordAnalyticEventUseCase(recorder)

    fun record(
        name: String,
        attributes: Map<String, String> = emptyMap(),
    ): AnalyticEventPayload = recordAnalyticEventUseCase(
        AnalyticEventPayload(name = name, attributes = attributes),
    )
}
