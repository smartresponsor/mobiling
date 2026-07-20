package app.mobiling.client.access

import androidx.compose.material3.Text
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
 * Behavioral coverage for the isolated Android access-entry surface.
 */
class AccessBehaviorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun guestEntryShowsSignInAndRegistrationActions() {
        composeRule.setContent {
            MobilingAppShell()
        }

        composeRule.onNodeWithText("SmartResponsor").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in").assertIsDisplayed()
        composeRule.onNodeWithText("Create access").assertIsDisplayed()
    }

    @Test
    fun guestCanOpenSignInAndReturnToWelcome() {
        composeRule.setContent {
            MobilingAppShell()
        }

        composeRule.onNodeWithText("Sign in").performClick()
        composeRule
            .onNodeWithText("Use your SmartResponsor access to enter the business workspace.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.onNodeWithText("Guest entry").assertIsDisplayed()
    }

    @Test
    fun guestCanOpenRegistrationAndReturnToWelcome() {
        composeRule.setContent {
            MobilingAppShell()
        }

        composeRule.onNodeWithText("Create access").performClick()
        composeRule
            .onNodeWithText("Set up a guest entry for the SmartResponsor workspace.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.onNodeWithText("Guest entry").assertIsDisplayed()
    }

    @Test
    fun guestCanOpenRecoveryRequestAndReturnToSignIn() {
        composeRule.setContent {
            MobilingAppShell()
        }

        composeRule.onNodeWithText("Sign in").performClick()
        composeRule.onNodeWithText("Recover access").performClick()
        composeRule
            .onNodeWithText("Request a recovery code for your SmartResponsor access.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()
        composeRule
            .onNodeWithText("Use your SmartResponsor access to enter the business workspace.")
            .assertIsDisplayed()
    }

    @Test
    fun guestCanMoveBetweenRecoveryRequestAndReset() {
        composeRule.setContent {
            MobilingAppShell()
        }

        composeRule.onNodeWithText("Sign in").performClick()
        composeRule.onNodeWithText("Recover access").performClick()
        composeRule.onNodeWithText("I have a recovery code").performClick()
        composeRule
            .onNodeWithText("Use your recovery code and choose a new password.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Request code").performClick()
        composeRule
            .onNodeWithText("Request a recovery code for your SmartResponsor access.")
            .assertIsDisplayed()
    }

    @Test
    fun restoredSessionRoutesToVerificationRequired() {
        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = bridgeFor(
                    session(requiresVerification = true),
                ),
            )
        }

        composeRule
            .onNodeWithText("Accessing requires identity verification before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun restoredSessionRoutesToSecondFactorRequired() {
        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = bridgeFor(
                    session(requiresSecondFactor = true),
                ),
            )
        }

        composeRule
            .onNodeWithText("Accessing requires an additional verification step before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun verificationTakesPriorityOverSecondFactorAndAuthenticatedState() {
        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = bridgeFor(
                    session(
                        authenticated = true,
                        requiresVerification = true,
                        requiresSecondFactor = true,
                    ),
                ),
                authenticatedContent = { vendorId, _ ->
                    Text("Authenticated vendor: $vendorId")
                },
            )
        }

        composeRule
            .onNodeWithText("Accessing requires identity verification before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun authenticatedSessionUsesInjectedContentAndVendorIdentity() {
        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = bridgeFor(
                    session(authenticated = true),
                ),
                authenticatedContent = { vendorId, _ ->
                    Text("Authenticated vendor: $vendorId")
                },
            )
        }

        composeRule
            .onNodeWithText("Authenticated vendor: test-vendor")
            .assertIsDisplayed()
    }

    private fun bridgeFor(payload: AccessAuthSessionPayload): AccessAuthFeatureBridge =
        AccessAuthFeatureBridge(FakeAccessAuthSessionGateway(payload))

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

private class FakeAccessAuthSessionGateway(
    private val payload: AccessAuthSessionPayload,
) : AccessAuthSessionGateway {
    override suspend fun startAuth(request: AccessStartAuthRequest): AccessAuthSessionPayload = payload

    override suspend fun registerAuth(request: AccessRegisterAuthRequest): AccessAuthSessionPayload = payload

    override suspend fun restoreAuth(): AccessAuthSessionPayload = payload

    override suspend fun logoutAuth() = Unit

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
