package app.mobiling.client.access

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import app.mobiling.client.auth.AccessAuthFeatureBridge
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
        val gateway = AccessAuthSessionGatewayFixture(
            recoveryRequestPayload = testSessionPayload(requiresVerification = true),
        )

        composeRule.setContent {
            MobilingAppShell(accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway))
        }

        submitRecoveryRequest()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.recoveryRequestCalls == 1 }
        check(gateway.recoveryRequest?.email == "user@example.com")
        composeRule
            .onNodeWithText("Accessing requires identity verification before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun recoveryRequestResponseRoutesToSecondFactorRequired() {
        val gateway = AccessAuthSessionGatewayFixture(
            recoveryRequestPayload = testSessionPayload(requiresSecondFactor = true),
        )

        composeRule.setContent {
            MobilingAppShell(accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway))
        }

        submitRecoveryRequest()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.recoveryRequestCalls == 1 }
        composeRule
            .onNodeWithText("Accessing requires an additional verification step before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun recoveryRequestResponseRoutesToAuthenticatedContentWithVendorIdentity() {
        val gateway = AccessAuthSessionGatewayFixture(
            recoveryRequestPayload = testSessionPayload(authenticated = true),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
                authenticatedContent = { vendorId, _ -> Text("Recovered vendor: $vendorId") },
            )
        }

        submitRecoveryRequest()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.recoveryRequestCalls == 1 }
        composeRule.onNodeWithText("Recovered vendor: test-vendor").assertIsDisplayed()
    }

    @Test
    fun unavailableRecoveryBridgeKeepsFormAndShowsStatus() {
        composeRule.setContent { MobilingAppShell() }

        submitRecoveryRequest()
        composeRule.onNodeWithText("Access service is unavailable.").assertIsDisplayed()
        assertRecoveryRequestFormDisplayed()
    }

    @Test
    fun recoveryRequestFailureKeepsFormAndShowsStatus() {
        val gateway = AccessAuthSessionGatewayFixture(
            recoveryRequestFailure = IllegalStateException("recovery unavailable"),
        )

        composeRule.setContent {
            MobilingAppShell(accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway))
        }

        submitRecoveryRequest()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.recoveryRequestCalls == 1 }
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
}
