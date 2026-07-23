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
    private val payload: AccessAuthSessionPayload = guestSessionPayload(),
    private val startPayload: AccessAuthSessionPayload = payload,
    private val restorePayload: AccessAuthSessionPayload = payload,
    private val registrationPayload: AccessAuthSessionPayload = payload,
    private val recoveryRequestPayload: AccessAuthSessionPayload = payload,
    private val recoveryResetPayload: AccessAuthSessionPayload = payload,
    private val startFailure: RuntimeException? = null,
    private val restoreFailure: RuntimeException? = null,
    private val logoutFailure: RuntimeException? = null,
    private val registrationFailure: RuntimeException? = null,
    private val recoveryRequestFailure: RuntimeException? = null,
    private val recoveryResetFailure: RuntimeException? = null,
) : AccessAuthSessionGateway {
    var startCalls: Int = 0
        private set

    var restoreCalls: Int = 0
        private set

    var logoutCalls: Int = 0
        private set

    var registrationCalls: Int = 0
        private set

    var recoveryRequestCalls: Int = 0
        private set

    var recoveryResetCalls: Int = 0
        private set

    var startRequest: AccessStartAuthRequest? = null
        private set

    var registrationRequest: AccessRegisterAuthRequest? = null
        private set

    var recoveryRequest: AccessRequestRecoveryRequest? = null
        private set

    var recoveryResetRequest: AccessResetRecoveryRequest? = null
        private set

    override suspend fun startAuth(request: AccessStartAuthRequest): AccessAuthSessionPayload {
        startCalls += 1
        startRequest = request
        startFailure?.let { throw it }

        return startPayload
    }

    override suspend fun registerAuth(request: AccessRegisterAuthRequest): AccessAuthSessionPayload {
        registrationCalls += 1
        registrationRequest = request
        registrationFailure?.let { throw it }

        return registrationPayload
    }

    override suspend fun restoreAuth(): AccessAuthSessionPayload {
        restoreCalls += 1
        restoreFailure?.let { throw it }

        return restorePayload
    }

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
    ): AccessAuthSessionPayload {
        recoveryRequestCalls += 1
        recoveryRequest = request
        recoveryRequestFailure?.let { throw it }

        return recoveryRequestPayload
    }

    override suspend fun resetRecovery(
        request: AccessResetRecoveryRequest,
    ): AccessAuthSessionPayload {
        recoveryResetCalls += 1
        recoveryResetRequest = request
        recoveryResetFailure?.let { throw it }

        return recoveryResetPayload
    }
}

internal fun guestSessionPayload(): AccessAuthSessionPayload = AccessAuthSessionPayload(
    status = "guest",
    sessionId = null,
    vendorId = null,
    authenticated = false,
    requiresVerification = false,
    requiresSecondFactor = false,
)

internal fun testSessionPayload(
    authenticated: Boolean = false,
    requiresVerification: Boolean = false,
    requiresSecondFactor: Boolean = false,
): AccessAuthSessionPayload = AccessAuthSessionPayload(
    status = "test",
    sessionId = "test-session",
    vendorId = "test-vendor",
    authenticated = authenticated,
    requiresVerification = requiresVerification,
    requiresSecondFactor = requiresSecondFactor,
)

internal fun verificationRequiredPayload(): AccessAuthSessionPayload = testSessionPayload(
    requiresVerification = true,
)

internal fun secondFactorRequiredPayload(): AccessAuthSessionPayload = testSessionPayload(
    requiresSecondFactor = true,
)
