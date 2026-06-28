package app.mobiling.core.analytic

import app.mobiling.client.contract.system.analytic.AnalyticEventPayload
import app.mobiling.client.data.system.analytic.AnalyticEventRecorder
import app.mobiling.client.usecase.system.analytic.AnalyticRecordEventUseCase

/**
 * Legacy-compatible Android entry point bridged to canonical system/analytic slices.
 */
class Analytic(
    private val recorder: AnalyticEventRecorder = AnalyticEventRecorder(),
) {
    private val analyticRecordEventUseCase: AnalyticRecordEventUseCase = AnalyticRecordEventUseCase(recorder)

    fun record(
        name: String,
        attributes: Map<String, String> = emptyMap(),
    ): AnalyticEventPayload = analyticRecordEventUseCase(
        AnalyticEventPayload(name = name, attributes = attributes),
    )
}
