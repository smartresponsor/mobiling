package app.mobiling.client.client.data.system.entitlement

import app.mobiling.client.client.contract.system.entitlement.EntitlementSnapshot

/**
 * Canonical data adapter responsible for loading entitlement state.
 */
class EntitlementSnapshotGateway {
    fun fetch(subjectId: String): EntitlementSnapshot = EntitlementSnapshot(subjectId = subjectId)
}
