package com.smartresponsor.core.entitlement

import com.smartresponsor.mobile.client.contract.system.entitlement.entitlementsnapshot
import com.smartresponsor.mobile.client.data.system.entitlement.entitlementsnapshotgateway
import com.smartresponsor.mobile.client.usecase.system.entitlement.refreshentitlementsnapshotusecase

/**
 * Legacy-compatible Android entry point bridged to canonical system/entitlement slices.
 */
class Entitlement(
    private val gateway: EntitlementSnapshotGateway = EntitlementSnapshotGateway(),
) {
    private val refreshEntitlementSnapshotUseCase: RefreshEntitlementSnapshotUseCase =
        RefreshEntitlementSnapshotUseCase(gateway)

    fun refresh(subjectId: String): EntitlementSnapshot = refreshEntitlementSnapshotUseCase(subjectId)
}
