package app.mobiling.client.access

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
 * Behavioral coverage for Android recovery-request response handling.
 */
class RecoveryRequestBehaviorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun recoveryRequestResponseRoutesToVerificationRequired() {
        val gateway = RecoveryRequestGateway(
            recoveryPayload = session(requiresVerification = true),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
            )
        }

        submitRecoveryRequest()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.recoveryCalls == 1 }
        composeRule
            .onNodeWithText("Accessing requires identity verification before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun recoveryRequestResponseRoutesToSecondFactorRequired() {
        val gateway = RecoveryRequestGateway(
            recoveryPayload = session(requiresSecondFactor = true),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
            )
        }

        submitRecoveryRequest()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.recoveryCalls == 1 }
        composeRule
            .onNodeWithText("Accessing requires an additional verification step before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun recoveryRequestResponseRoutesToAuthenticatedContentWithVendorIdentity() {
        val gateway = RecoveryRequestGateway(
            recoveryPayload = session(authenticated = true),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
                authenticatedContent = { vendorId, _ ->
                    Text("Recovered vendor: $vendorId")
                },
            )
        }

        submitRecoveryRequest()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.recoveryCalls == 1 }
        composeRule.onNodeWithText("Recovered vendor: test-vendor").assertIsDisplayed()
    }

    @Test
    fun unavailableRecoveryBridgeKeepsFormAndShowsStatus() {
        composeRule.setContent {
            MobilingAppShell()
        }

        submitRecoveryRequest()
        composeRule.onNodeWithText("Access service is unavailable.").assertIsDisplayed()
        assertRecoveryRequestFormDisplayed()
    }

    @Test
    fun recoveryRequestFailureKeepsFormAndShowsStatus() {
        val gateway = RecoveryRequestGateway(
            recoveryFailure = IllegalStateException("recovery unavailable"),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
            )
        }

        submitRecoveryRequest()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.recoveryCalls == 1 }
        composeRule.onNodeWithText("Recovery request could not be started.").assertIsDisplayed()
        assertRecoveryRequestFormDisplayed()
    }

    private fun submitRecoveryRequest() {
        composeRule.onNodeWithText("Sign in").performClick()
        composeRule.onNodeWithText("Recover access").performClick()
        composeRule.onNodeWithText("Email").performTextInput("user@example.com")
        composeRule.onNodeWithText("Send recovery code").performClick()
    }

    private fun assertRecoveryRequestFormDisplayed() {
        composeRule.onNodeWithText("Email").assertIsDisplayed()
        composeRule.onNodeWithText("Send recovery code").assertIsDisplayed()
        composeRule.onNodeWithText("I have a recovery code").assertIsDisplayed()
        composeRule
            .onNodeWithText("Request a recovery code for your SmartResponsor access.")
            .assertIsDisplayed()
    }

    private fun session(
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
}

private class RecoveryRequestGateway(
    private val recoveryPayload: AccessAuthSessionPayload = guestSession(),
    private val recoveryFailure: RuntimeException? = null,
) : AccessAuthSessionGateway {
    var recoveryCalls: Int = 0
        private set

    override suspend fun startAuth(request: AccessStartAuthRequest): AccessAuthSessionPayload = recoveryPayload

    override suspend fun registerAuth(request: AccessRegisterAuthRequest): AccessAuthSessionPayload = recoveryPayload

    override suspend fun restoreAuth(): AccessAuthSessionPayload = guestSession()

    override suspend fun logoutAuth() = Unit

    override suspend fun resendVerification(): AccessAuthSessionPayload = recoveryPayload

    override suspend fun confirmVerification(
        request: AccessConfirmVerificationRequest,
    ): AccessAuthSessionPayload = recoveryPayload

    override suspend fun challengeSecondFactor(): AccessAuthSessionPayload = recoveryPayload

    override suspend fun verifySecondFactor(
        request: AccessVerifySecondFactorRequest,
    ): AccessAuthSessionPayload = recoveryPayload

    override suspend fun requestRecovery(
        request: AccessRequestRecoveryRequest,
    ): AccessAuthSessionPayload {
        recoveryCalls += 1
        recoveryFailure?.let { throw it }

        return recoveryPayload
    }

    override suspend fun resetRecovery(
        request: AccessResetRecoveryRequest,
    ): AccessAuthSessionPayload = recoveryPayload
}

private fun guestSession(): AccessAuthSessionPayload = AccessAuthSessionPayload(
    status = "guest",
    sessionId = null,
    vendorId = null,
    authenticated = false,
    requiresVerification = false,
    requiresSecondFactor = false,
)
