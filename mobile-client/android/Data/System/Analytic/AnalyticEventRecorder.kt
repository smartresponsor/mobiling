package com.smartresponsor.mobile.client.data.system.analytic

import com.smartresponsor.mobile.client.contract.system.analytic.AnalyticEventPayload

/**
 * Canonical data adapter responsible for recording analytics events.
 */
class AnalyticEventRecorder {
    fun record(payload: AnalyticEventPayload): AnalyticEventPayload = payload
}
