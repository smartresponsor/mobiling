package app.mobiling.client.usecase.system.entitlement

import app.mobiling.client.contract.system.entitlement.EntitlementSnapshot
import app.mobiling.client.data.system.entitlement.EntitlementSnapshotGateway

/**
 * Canonical use case for refreshing entitlement state.
 */
class EntitlementRefreshSnapshotUseCase(
    private val gateway: EntitlementSnapshotGateway = EntitlementSnapshotGateway(),
) {
    operator fun invoke(subjectId: String): EntitlementSnapshot = gateway.fetch(subjectId)
}
