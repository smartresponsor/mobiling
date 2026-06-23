package app.mobiling.client.usecase.system.analytic

import app.mobiling.client.contract.system.analytic.AnalyticEventPayload
import app.mobiling.client.data.system.analytic.AnalyticEventRecorder

/**
 * Canonical use case for analytics event emission.
 */
class RecordAnalyticEventUseCase(
    private val recorder: AnalyticEventRecorder = AnalyticEventRecorder(),
) {
    operator fun invoke(payload: AnalyticEventPayload): AnalyticEventPayload = recorder.record(payload)
}
