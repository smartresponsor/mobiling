package com.smartresponsor.mobile.client.usecase.system.entitlement

import com.smartresponsor.mobile.client.contract.system.entitlement.entitlementsnapshot
import com.smartresponsor.mobile.client.data.system.entitlement.entitlementsnapshotgateway

/**
 * Canonical use case for refreshing entitlement state.
 */
class RefreshEntitlementSnapshotUseCase(
    private val gateway: EntitlementSnapshotGateway = EntitlementSnapshotGateway(),
) {
    operator fun invoke(subjectId: String): EntitlementSnapshot = gateway.fetch(subjectId)
}
