package app.mobiling.client.client.usecase.system.entitlement

import app.mobiling.client.client.contract.system.entitlement.EntitlementSnapshot
import app.mobiling.client.client.data.system.entitlement.EntitlementSnapshotGateway

/**
 * Canonical use case for refreshing entitlement state.
 */
class RefreshEntitlementSnapshotUseCase(
    private val gateway: EntitlementSnapshotGateway = EntitlementSnapshotGateway(),
) {
    operator fun invoke(subjectId: String): EntitlementSnapshot = gateway.fetch(subjectId)
}
