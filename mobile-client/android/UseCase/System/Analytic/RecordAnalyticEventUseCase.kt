package com.smartresponsor.mobile.client.usecase.system.analytic

import com.smartresponsor.mobile.client.contract.system.analytic.AnalyticEventPayload
import com.smartresponsor.mobile.client.data.system.analytic.AnalyticEventRecorder

/**
 * Canonical use case for analytics event emission.
 */
class RecordAnalyticEventUseCase(
    private val recorder: AnalyticEventRecorder = AnalyticEventRecorder(),
) {
    operator fun invoke(payload: AnalyticEventPayload): AnalyticEventPayload = recorder.record(payload)
}
