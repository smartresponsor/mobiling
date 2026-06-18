package com.smartresponsor.mobile.client.contract.system.entitlement

/**
 * Canonical contract snapshot for client entitlement state.
 */
data class EntitlementSnapshot(
    val subjectId: String,
    val grantedCodes: List<String> = emptyList(),
    val refreshedAtIso8601: String? = null,
)
