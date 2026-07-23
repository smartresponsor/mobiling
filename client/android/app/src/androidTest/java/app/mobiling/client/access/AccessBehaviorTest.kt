package app.mobiling.client.access

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import app.mobiling.client.auth.AccessAuthFeatureBridge
import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
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
        composeRule.setContent { MobilingAppShell() }

        composeRule.onNodeWithText("SmartResponsor").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in").assertIsDisplayed()
        composeRule.onNodeWithText("Create access").assertIsDisplayed()
    }

    @Test
    fun guestCanOpenSignInAndReturnToWelcome() {
        composeRule.setContent { MobilingAppShell() }

        composeRule.onNodeWithText("Sign in").performClick()
        composeRule
            .onNodeWithText("Use your SmartResponsor access to enter the business workspace.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.onNodeWithText("Guest entry").assertIsDisplayed()
    }

    @Test
    fun guestCanOpenRegistrationAndReturnToWelcome() {
        composeRule.setContent { MobilingAppShell() }

        composeRule.onNodeWithText("Create access").performClick()
        composeRule
            .onNodeWithText("Set up a guest entry for the SmartResponsor workspace.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.onNodeWithText("Guest entry").assertIsDisplayed()
    }

    @Test
    fun guestCanOpenRecoveryRequestAndReturnToSignIn() {
        composeRule.setContent { MobilingAppShell() }

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
        composeRule.setContent { MobilingAppShell() }

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
                accessAuthFeatureBridge = bridgeFor(testSessionPayload(requiresVerification = true)),
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
                accessAuthFeatureBridge = bridgeFor(testSessionPayload(requiresSecondFactor = true)),
            )
        }

        composeRule
            .onNodeWithText("Accessing requires an additional verification step before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun restoreGuestPayloadKeepsGuestEntryAvailable() {
        composeRule.setContent {
            MobilingAppShell(accessAuthFeatureBridge = bridgeFor(guestSessionPayload()))
        }

        composeRule.onNodeWithText("Guest entry").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in").assertIsDisplayed()
        composeRule.onNodeWithText("Create access").assertIsDisplayed()
    }

    @Test
    fun restoreFailureKeepsGuestEntryAvailable() {
        val gateway = AccessAuthSessionGatewayFixture(
            payload = guestSessionPayload(),
            restoreFailure = IllegalStateException("restore unavailable"),
        )

        composeRule.setContent {
            MobilingAppShell(accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway))
        }

        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.restoreCalls == 1 }
        composeRule.onNodeWithText("Guest entry").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in").assertIsDisplayed()
        composeRule.onNodeWithText("Create access").assertIsDisplayed()
    }

    @Test
    fun signInResponseRoutesToVerificationRequired() {
        val gateway = AccessAuthSessionGatewayFixture(
            payload = guestSessionPayload(),
            startPayload = verificationRequiredPayload(),
        )

        composeRule.setContent {
            MobilingAppShell(accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway))
        }

        submitSignIn()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.startCalls == 1 }
        composeRule
            .onNodeWithText("Accessing requires identity verification before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun signInResponseRoutesToSecondFactorRequired() {
        val gateway = AccessAuthSessionGatewayFixture(
            payload = guestSessionPayload(),
            startPayload = secondFactorRequiredPayload(),
        )

        composeRule.setContent {
            MobilingAppShell(accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway))
        }

        submitSignIn()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.startCalls == 1 }
        composeRule
            .onNodeWithText("Accessing requires an additional verification step before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun signInResponseRoutesToAuthenticatedContentWithVendorIdentity() {
        val gateway = AccessAuthSessionGatewayFixture(
            payload = guestSessionPayload(),
            startPayload = testSessionPayload(authenticated = true),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
                authenticatedContent = { vendorId, _ -> Text("Authenticated vendor: $vendorId") },
            )
        }

        submitSignIn()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.startCalls == 1 }
        composeRule.onNodeWithText("Authenticated vendor: test-vendor").assertIsDisplayed()
    }

    @Test
    fun unavailableSignInBridgeKeepsFormAndShowsStatus() {
        composeRule.setContent { MobilingAppShell() }

        submitSignIn()
        composeRule.onNodeWithText("Access service is unavailable.").assertIsDisplayed()
        assertSignInFormDisplayed()
    }

    @Test
    fun signInFailureKeepsFormAndShowsStatus() {
        val gateway = AccessAuthSessionGatewayFixture(
            payload = guestSessionPayload(),
            startFailure = IllegalStateException("sign in unavailable"),
        )

        composeRule.setContent {
            MobilingAppShell(accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway))
        }

        submitSignIn()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.startCalls == 1 }
        composeRule.onNodeWithText("Access session could not be started.").assertIsDisplayed()
        assertSignInFormDisplayed()
    }

    @Test
    fun verificationTakesPriorityOverSecondFactorAndAuthenticatedState() {
        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = bridgeFor(
                    testSessionPayload(
                        authenticated = true,
                        requiresVerification = true,
                        requiresSecondFactor = true,
                    ),
                ),
                authenticatedContent = { vendorId, _ -> Text("Authenticated vendor: $vendorId") },
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
                accessAuthFeatureBridge = bridgeFor(testSessionPayload(authenticated = true)),
                authenticatedContent = { vendorId, _ -> Text("Authenticated vendor: $vendorId") },
            )
        }

        composeRule.onNodeWithText("Authenticated vendor: test-vendor").assertIsDisplayed()
    }

    @Test
    fun authenticatedSignOutLogsOutAndReturnsToGuestEntry() {
        val gateway = AccessAuthSessionGatewayFixture(
            payload = testSessionPayload(authenticated = true),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
                authenticatedContent = { vendorId, onSignOut ->
                    Button(onClick = onSignOut) { Text("Sign out vendor: $vendorId") }
                },
            )
        }

        composeRule.onNodeWithText("Sign out vendor: test-vendor").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.logoutCalls == 1 }
        composeRule.onNodeWithText("Guest entry").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in").assertIsDisplayed()
    }

    @Test
    fun logoutFailureStillClearsLocalSessionAndReturnsToGuestEntry() {
        val gateway = AccessAuthSessionGatewayFixture(
            payload = testSessionPayload(authenticated = true),
            logoutFailure = IllegalStateException("logout unavailable"),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
                authenticatedContent = { vendorId, onSignOut ->
                    Button(onClick = onSignOut) { Text("Sign out vendor: $vendorId") }
                },
            )
        }

        composeRule.onNodeWithText("Sign out vendor: test-vendor").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.logoutCalls == 1 }
        composeRule.onNodeWithText("Guest entry").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in").assertIsDisplayed()
    }

    private fun submitSignIn() {
        composeRule.onNodeWithText("Sign in").performClick()
        composeRule.onNodeWithText("Email").performTextInput("user@example.com")
        composeRule.onNodeWithText("Password").performTextInput("password")
        composeRule.onNodeWithText("Sign in").performClick()
    }

    private fun assertSignInFormDisplayed() {
        composeRule.onNodeWithText("Email").assertIsDisplayed()
        composeRule.onNodeWithText("Password").assertIsDisplayed()
        composeRule
            .onNodeWithText("Use your SmartResponsor access to enter the business workspace.")
            .assertIsDisplayed()
    }

    private fun bridgeFor(payload: AccessAuthSessionPayload): AccessAuthFeatureBridge =
        AccessAuthFeatureBridge(AccessAuthSessionGatewayFixture(payload = payload))
}
