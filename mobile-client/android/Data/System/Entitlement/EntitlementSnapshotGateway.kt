package com.smartresponsor.mobile.client.data.system.entitlement

import com.smartresponsor.mobile.client.contract.system.entitlement.entitlementsnapshot

/**
 * Canonical data adapter responsible for loading entitlement state.
 */
class EntitlementSnapshotGateway {
    fun fetch(subjectId: String): EntitlementSnapshot = EntitlementSnapshot(subjectId = subjectId)
}
