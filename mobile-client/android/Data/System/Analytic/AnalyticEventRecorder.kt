package app.mobiling.client.client.data.system.analytic

import app.mobiling.client.client.contract.system.analytic.AnalyticEventPayload

/**
 * Canonical data adapter responsible for recording analytics events.
 */
class AnalyticEventRecorder {
    fun record(payload: AnalyticEventPayload): AnalyticEventPayload = payload
}
