package app.mobiling.client.access

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.mobiling.client.auth.AccessAuthFeatureBridge
import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.contract.auth.session.AccessConfirmVerificationRequest
import app.mobiling.client.contract.auth.session.AccessRegisterAuthRequest
import app.mobiling.client.contract.auth.session.AccessRequestRecoveryRequest
import app.mobiling.client.contract.auth.session.AccessResetRecoveryRequest
import app.mobiling.client.contract.auth.session.AccessStartAuthRequest
import app.mobiling.client.contract.auth.session.AccessVerifySecondFactorRequest
import app.mobiling.client.data.auth.session.AccessAuthSessionGateway
import org.junit.Rule
import org.junit.Test

/**
 * Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp.
 *
 * Behavioral coverage for Android verification-state navigation.
 */
class VerificationBehaviorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun checkAgainReturnsToSignInWithoutLoggingOut() {
        val gateway = VerificationGateway()

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
            )
        }

        composeRule
            .onNodeWithText("Accessing requires identity verification before this mobile session can continue.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Check again").performClick()

        composeRule
            .onNodeWithText("Use your SmartResponsor access to enter the business workspace.")
            .assertIsDisplayed()
        check(gateway.logoutCalls == 0)
    }

    @Test
    fun useDifferentAccessLogsOutAndReturnsToGuestEntry() {
        val gateway = VerificationGateway()

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
            )
        }

        composeRule.onNodeWithText("Use different access").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.logoutCalls == 1 }
        composeRule.onNodeWithText("Guest entry").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in").assertIsDisplayed()
        composeRule.onNodeWithText("Create access").assertIsDisplayed()
    }

    @Test
    fun logoutFailureStillReturnsVerificationUserToGuestEntry() {
        val gateway = VerificationGateway(
            logoutFailure = IllegalStateException("logout unavailable"),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
            )
        }

        composeRule.onNodeWithText("Use different access").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.logoutCalls == 1 }
        composeRule.onNodeWithText("Guest entry").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in").assertIsDisplayed()
    }
}

private class VerificationGateway(
    private val logoutFailure: RuntimeException? = null,
) : AccessAuthSessionGateway {
    private val verificationPayload = AccessAuthSessionPayload(
        status = "verification_required",
        sessionId = "test-session",
        vendorId = "test-vendor",
        authenticated = false,
        requiresVerification = true,
        requiresSecondFactor = false,
    )

    var logoutCalls: Int = 0
        private set

    override suspend fun startAuth(request: AccessStartAuthRequest): AccessAuthSessionPayload =
        verificationPayload

    override suspend fun registerAuth(request: AccessRegisterAuthRequest): AccessAuthSessionPayload =
        verificationPayload

    override suspend fun restoreAuth(): AccessAuthSessionPayload = verificationPayload

    override suspend fun logoutAuth() {
        logoutCalls += 1
        logoutFailure?.let { throw it }
    }

    override suspend fun resendVerification(): AccessAuthSessionPayload = verificationPayload

    override suspend fun confirmVerification(
        request: AccessConfirmVerificationRequest,
    ): AccessAuthSessionPayload = verificationPayload

    override suspend fun challengeSecondFactor(): AccessAuthSessionPayload = verificationPayload

    override suspend fun verifySecondFactor(
        request: AccessVerifySecondFactorRequest,
    ): AccessAuthSessionPayload = verificationPayload

    override suspend fun requestRecovery(
        request: AccessRequestRecoveryRequest,
    ): AccessAuthSessionPayload = verificationPayload

    override suspend fun resetRecovery(
        request: AccessResetRecoveryRequest,
    ): AccessAuthSessionPayload = verificationPayload
}
