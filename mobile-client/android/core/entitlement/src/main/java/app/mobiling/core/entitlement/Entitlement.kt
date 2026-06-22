package app.mobiling.core.entitlement

import app.mobiling.client.client.contract.system.entitlement.EntitlementSnapshot
import app.mobiling.client.client.data.system.entitlement.EntitlementSnapshotGateway
import app.mobiling.client.client.usecase.system.entitlement.RefreshEntitlementSnapshotUseCase

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
