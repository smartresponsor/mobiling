package app.mobiling.core.entitlement

import app.mobiling.client.contract.system.entitlement.EntitlementSnapshot
import app.mobiling.client.data.system.entitlement.EntitlementSnapshotGateway
import app.mobiling.client.usecase.system.entitlement.EntitlementRefreshSnapshotUseCase

/**
 * Legacy-compatible Android entry point bridged to canonical system/entitlement slices.
 */
class Entitlement(
    private val gateway: EntitlementSnapshotGateway = EntitlementSnapshotGateway(),
) {
    private val entitlementRefreshSnapshotUseCase: EntitlementRefreshSnapshotUseCase =
        EntitlementRefreshSnapshotUseCase(gateway)

    fun refresh(subjectId: String): EntitlementSnapshot = entitlementRefreshSnapshotUseCase(subjectId)
}
