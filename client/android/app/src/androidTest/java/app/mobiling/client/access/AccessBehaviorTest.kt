package app.mobiling.client.access

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.AnnotatedString
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

        composeRule.onNodeWithText("Your Trusted Home Specialist").assertIsDisplayed()
        composeRule.assertGuestEntryDisplayed(includeRegistrationAction = true)
    }

    @Test
    fun guestCanOpenSignInAndReturnToWelcome() {
        composeRule.setContent { MobilingAppShell() }

        composeRule.performAccessAction("Sign in")
        composeRule.assertSignInDisplayed()
        composeRule.onNodeWithText("Return to access welcome").performClick()
        composeRule.assertGuestEntryDisplayed()
    }

    @Test
    fun guestCanOpenRegistrationAndReturnToWelcome() {
        composeRule.setContent { MobilingAppShell() }

        composeRule.performAccessAction("Create account")
        composeRule
            .onNodeWithText("Set up a guest entry for the 1tasker workspace.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Return to access welcome").performClick()
        composeRule.assertGuestEntryDisplayed()
    }

    @Test
    fun guestCanOpenRecoveryRequestAndReturnToSignIn() {
        composeRule.setContent { MobilingAppShell() }

        composeRule.performAccessAction("Sign in")
        composeRule.onNodeWithText("Recover access").performClick()
        composeRule
            .onNodeWithText("Request a recovery code for your SmartResponsor access.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Return to access welcome").performClick()
        composeRule.assertSignInDisplayed()
    }

    @Test
    fun guestCanMoveBetweenRecoveryRequestAndReset() {
        composeRule.setContent { MobilingAppShell() }

        composeRule.performAccessAction("Sign in")
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

        composeRule.assertVerificationRequiredDisplayed()
    }

    @Test
    fun restoredSessionRoutesToSecondFactorRequired() {
        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = bridgeFor(testSessionPayload(requiresSecondFactor = true)),
            )
        }

        composeRule.assertSecondFactorRequiredDisplayed()
    }

    @Test
    fun restoreGuestPayloadKeepsGuestEntryAvailable() {
        val gateway = AccessAuthSessionGatewayFixture(payload = guestSessionPayload())

        composeRule.setAccessShell(gateway)

        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.restoreCalls == 1 }
        composeRule.assertGuestEntryDisplayed(includeRegistrationAction = true)
    }

    @Test
    fun restoreFailureKeepsGuestEntryAvailable() {
        val gateway = AccessAuthSessionGatewayFixture(
            payload = guestSessionPayload(),
            restoreFailure = IllegalStateException("restore unavailable"),
        )

        composeRule.setAccessShell(gateway)

        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.restoreCalls == 1 }
        composeRule.assertGuestEntryDisplayed(includeRegistrationAction = true)
    }

    @Test
    fun emptySignInShowsFieldErrorsWithoutCallingGateway() {
        val gateway = AccessAuthSessionGatewayFixture(payload = guestSessionPayload())

        composeRule.setAccessShell(gateway)
        composeRule.performAccessAction("Sign in")
        composeRule.performAccessAction("Sign in")

        composeRule.onNodeWithText("Enter your email address.").assertIsDisplayed()
        composeRule.onNodeWithText("Enter your password.").assertIsDisplayed()
        composeRule.onNodeWithText("Check the highlighted fields and try again.").assertIsDisplayed()
        check(gateway.startCalls == 0)
    }

    @Test
    fun signInResponseRoutesToVerificationRequired() {
        val gateway = AccessAuthSessionGatewayFixture(
            payload = guestSessionPayload(),
            startPayload = verificationRequiredPayload(),
        )

        composeRule.setAccessShell(gateway)

        submitSignIn()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.startCalls == 1 }
        composeRule.assertVerificationRequiredDisplayed()
    }

    @Test
    fun signInResponseRoutesToSecondFactorRequired() {
        val gateway = AccessAuthSessionGatewayFixture(
            payload = guestSessionPayload(),
            startPayload = secondFactorRequiredPayload(),
        )

        composeRule.setAccessShell(gateway)

        submitSignIn()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.startCalls == 1 }
        composeRule.assertSecondFactorRequiredDisplayed()
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
        check(gateway.startRequest?.login == "user@example.com")
        check(gateway.startRequest?.password == "password")
        check(gateway.startRequest?.deviceLabel == "Android")
        composeRule.onNodeWithText("Authenticated vendor: test-vendor").assertIsDisplayed()
    }

    @Test
    fun unavailableSignInBridgeKeepsFormAndShowsStatus() {
        composeRule.setContent { MobilingAppShell() }

        submitSignIn()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(AccessUnavailableMessage)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText(AccessUnavailableMessage).assertIsDisplayed()
        assertSignInFormDisplayed()
    }

    @Test
    fun signInFailureKeepsFormAndShowsStatus() {
        val gateway = AccessAuthSessionGatewayFixture(
            payload = guestSessionPayload(),
            startFailure = IllegalStateException("sign in unavailable"),
        )

        composeRule.setAccessShell(gateway)

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

        composeRule.assertVerificationRequiredDisplayed()
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
        composeRule.assertGuestEntryDisplayed()
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
        composeRule.assertGuestEntryDisplayed()
    }

    private fun submitSignIn() {
        composeRule.performAccessAction("Sign in")
        composeRule.onNodeWithTag("access-sign-in-email").performSemanticsAction(SemanticsActions.SetText) {
            it(AnnotatedString("user@example.com"))
        }
        composeRule.onNodeWithTag("access-sign-in-password").performSemanticsAction(SemanticsActions.SetText) {
            it(AnnotatedString("password"))
        }
        composeRule.performAccessAction("Sign in")
    }

    private fun assertSignInFormDisplayed() {
        composeRule.onNodeWithText("Email").assertIsDisplayed()
        composeRule.onNodeWithText("Password").assertIsDisplayed()
        composeRule.assertSignInDisplayed()
    }

    private fun bridgeFor(payload: AccessAuthSessionPayload): AccessAuthFeatureBridge =
        AccessAuthFeatureBridge(AccessAuthSessionGatewayFixture(payload = payload))
}
