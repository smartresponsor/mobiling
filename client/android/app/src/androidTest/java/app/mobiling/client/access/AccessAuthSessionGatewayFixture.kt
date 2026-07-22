package app.mobiling.client.access

import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.contract.auth.session.AccessConfirmVerificationRequest
import app.mobiling.client.contract.auth.session.AccessRegisterAuthRequest
import app.mobiling.client.contract.auth.session.AccessRequestRecoveryRequest
import app.mobiling.client.contract.auth.session.AccessResetRecoveryRequest
import app.mobiling.client.contract.auth.session.AccessStartAuthRequest
import app.mobiling.client.contract.auth.session.AccessVerifySecondFactorRequest
import app.mobiling.client.data.auth.session.AccessAuthSessionGateway

/**
 * Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp.
 *
 * Reusable Android instrumentation fixture for access-session routing tests.
 */
internal class AccessAuthSessionGatewayFixture(
    private val payload: AccessAuthSessionPayload,
    private val logoutFailure: RuntimeException? = null,
) : AccessAuthSessionGateway {
    var logoutCalls: Int = 0
        private set

    override suspend fun startAuth(request: AccessStartAuthRequest): AccessAuthSessionPayload = payload

    override suspend fun registerAuth(request: AccessRegisterAuthRequest): AccessAuthSessionPayload = payload

    override suspend fun restoreAuth(): AccessAuthSessionPayload = payload

    override suspend fun logoutAuth() {
        logoutCalls += 1
        logoutFailure?.let { throw it }
    }

    override suspend fun resendVerification(): AccessAuthSessionPayload = payload

    override suspend fun confirmVerification(
        request: AccessConfirmVerificationRequest,
    ): AccessAuthSessionPayload = payload

    override suspend fun challengeSecondFactor(): AccessAuthSessionPayload = payload

    override suspend fun verifySecondFactor(
        request: AccessVerifySecondFactorRequest,
    ): AccessAuthSessionPayload = payload

    override suspend fun requestRecovery(
        request: AccessRequestRecoveryRequest,
    ): AccessAuthSessionPayload = payload

    override suspend fun resetRecovery(
        request: AccessResetRecoveryRequest,
    ): AccessAuthSessionPayload = payload
}

internal fun verificationRequiredPayload(): AccessAuthSessionPayload = AccessAuthSessionPayload(
    status = "verification_required",
    sessionId = "test-session",
    vendorId = "test-vendor",
    authenticated = false,
    requiresVerification = true,
    requiresSecondFactor = false,
)

internal fun secondFactorRequiredPayload(): AccessAuthSessionPayload = AccessAuthSessionPayload(
    status = "second_factor_required",
    sessionId = "test-session",
    vendorId = "test-vendor",
    authenticated = false,
    requiresVerification = false,
    requiresSecondFactor = true,
)
