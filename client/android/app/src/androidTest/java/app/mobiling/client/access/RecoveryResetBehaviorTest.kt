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
 * Behavioral coverage for Android recovery-reset response handling.
 */
class RecoveryResetBehaviorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun recoveryResetResponseRoutesToVerificationRequired() {
        val gateway = AccessAuthSessionGatewayFixture(
            recoveryResetPayload = testSessionPayload(requiresVerification = true),
        )

        composeRule.setContent {
            MobilingAppShell(accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway))
        }

        submitRecoveryReset()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.recoveryResetCalls == 1 }
        check(gateway.recoveryResetRequest?.email == "user@example.com")
        check(gateway.recoveryResetRequest?.code == "123456")
        check(gateway.recoveryResetRequest?.password == "new-password")
        composeRule
            .onNodeWithText("Accessing requires identity verification before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun recoveryResetResponseRoutesToSecondFactorRequired() {
        val gateway = AccessAuthSessionGatewayFixture(
            recoveryResetPayload = testSessionPayload(requiresSecondFactor = true),
        )

        composeRule.setContent {
            MobilingAppShell(accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway))
        }

        submitRecoveryReset()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.recoveryResetCalls == 1 }
        composeRule
            .onNodeWithText("Accessing requires an additional verification step before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun recoveryResetResponseRoutesToAuthenticatedContentWithVendorIdentity() {
        val gateway = AccessAuthSessionGatewayFixture(
            recoveryResetPayload = testSessionPayload(authenticated = true),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
                authenticatedContent = { vendorId, _ -> Text("Recovered vendor: $vendorId") },
            )
        }

        submitRecoveryReset()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.recoveryResetCalls == 1 }
        composeRule.onNodeWithText("Recovered vendor: test-vendor").assertIsDisplayed()
    }

    @Test
    fun unavailableRecoveryResetBridgeKeepsFormAndShowsStatus() {
        composeRule.setContent { MobilingAppShell() }

        submitRecoveryReset()
        composeRule.onNodeWithText("Access service is unavailable.").assertIsDisplayed()
        assertRecoveryResetFormDisplayed()
    }

    @Test
    fun recoveryResetFailureKeepsFormAndShowsStatus() {
        val gateway = AccessAuthSessionGatewayFixture(
            recoveryResetFailure = IllegalStateException("recovery reset unavailable"),
        )

        composeRule.setContent {
            MobilingAppShell(accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway))
        }

        submitRecoveryReset()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.recoveryResetCalls == 1 }
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
}
