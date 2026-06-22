package app.mobiling.client.contract.system.config

data class RemoteConfigPayload(
    val ttlSec: Int,
    val analyticEnabled: Boolean,
    val billingGraceDay: Int,
    val securitySigningEnabled: Boolean,
)
