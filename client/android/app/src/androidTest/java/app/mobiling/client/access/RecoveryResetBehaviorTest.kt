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
 * Behavioral coverage for Android recovery-reset response handling.
 */
class RecoveryResetBehaviorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun recoveryResetResponseRoutesToVerificationRequired() {
        val gateway = RecoveryResetGateway(
            resetPayload = session(requiresVerification = true),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
            )
        }

        submitRecoveryReset()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.resetCalls == 1 }
        composeRule
            .onNodeWithText("Accessing requires identity verification before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun recoveryResetResponseRoutesToSecondFactorRequired() {
        val gateway = RecoveryResetGateway(
            resetPayload = session(requiresSecondFactor = true),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
            )
        }

        submitRecoveryReset()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.resetCalls == 1 }
        composeRule
            .onNodeWithText("Accessing requires an additional verification step before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun recoveryResetResponseRoutesToAuthenticatedContentWithVendorIdentity() {
        val gateway = RecoveryResetGateway(
            resetPayload = session(authenticated = true),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
                authenticatedContent = { vendorId, _ ->
                    Text("Recovered vendor: $vendorId")
                },
            )
        }

        submitRecoveryReset()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.resetCalls == 1 }
        composeRule.onNodeWithText("Recovered vendor: test-vendor").assertIsDisplayed()
    }

    @Test
    fun unavailableRecoveryResetBridgeKeepsFormAndShowsStatus() {
        composeRule.setContent {
            MobilingAppShell()
        }

        submitRecoveryReset()
        composeRule.onNodeWithText("Access service is unavailable.").assertIsDisplayed()
        assertRecoveryResetFormDisplayed()
    }

    @Test
    fun recoveryResetFailureKeepsFormAndShowsStatus() {
        val gateway = RecoveryResetGateway(
            resetFailure = IllegalStateException("recovery reset unavailable"),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
            )
        }

        submitRecoveryReset()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.resetCalls == 1 }
        composeRule.onNodeWithText("Recovery reset could not be completed.").assertIsDisplayed()
        assertRecoveryResetFormDisplayed()
    }

    private fun submitRecoveryReset() {
        composeRule.onNodeWithText("Sign in").performClick()
        composeRule.onNodeWithText("Recover access").performClick()
        composeRule.onNodeWithText("I have a recovery code").performClick()
        composeRule.onNodeWithText("Email").performTextInput("user@example.com")
        composeRule.onNodeWithText("Recovery code").performTextInput("123456")
        composeRule.onNodeWithText("New password").performTextInput("new-password")
        composeRule.onNodeWithText("Reset access").performClick()
    }

    private fun assertRecoveryResetFormDisplayed() {
        composeRule.onNodeWithText("Email").assertIsDisplayed()
        composeRule.onNodeWithText("Recovery code").assertIsDisplayed()
        composeRule.onNodeWithText("New password").assertIsDisplayed()
        composeRule.onNodeWithText("Reset access").assertIsDisplayed()
        composeRule.onNodeWithText("Request code").assertIsDisplayed()
        composeRule
            .onNodeWithText("Use your recovery code and choose a new password.")
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

private class RecoveryResetGateway(
    private val resetPayload: AccessAuthSessionPayload = guestSession(),
    private val resetFailure: RuntimeException? = null,
) : AccessAuthSessionGateway {
    var resetCalls: Int = 0
        private set

    override suspend fun startAuth(request: AccessStartAuthRequest): AccessAuthSessionPayload = resetPayload

    override suspend fun registerAuth(request: AccessRegisterAuthRequest): AccessAuthSessionPayload = resetPayload

    override suspend fun restoreAuth(): AccessAuthSessionPayload = guestSession()

    override suspend fun logoutAuth() = Unit

    override suspend fun resendVerification(): AccessAuthSessionPayload = resetPayload

    override suspend fun confirmVerification(
        request: AccessConfirmVerificationRequest,
    ): AccessAuthSessionPayload = resetPayload

    override suspend fun challengeSecondFactor(): AccessAuthSessionPayload = resetPayload

    override suspend fun verifySecondFactor(
        request: AccessVerifySecondFactorRequest,
    ): AccessAuthSessionPayload = resetPayload

    override suspend fun requestRecovery(
        request: AccessRequestRecoveryRequest,
    ): AccessAuthSessionPayload = resetPayload

    override suspend fun resetRecovery(
        request: AccessResetRecoveryRequest,
    ): AccessAuthSessionPayload {
        resetCalls += 1
        resetFailure?.let { throw it }

        return resetPayload
    }
}

private fun guestSession(): AccessAuthSessionPayload = AccessAuthSessionPayload(
    status = "guest",
    sessionId = null,
    vendorId = null,
    authenticated = false,
    requiresVerification = false,
    requiresSecondFactor = false,
)
