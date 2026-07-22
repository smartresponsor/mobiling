package app.mobiling.client.access

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.mobiling.client.auth.AccessAuthFeatureBridge
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
        val gateway = AccessAuthSessionGatewayFixture(
            payload = verificationRequiredPayload(),
        )

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
        val gateway = AccessAuthSessionGatewayFixture(
            payload = verificationRequiredPayload(),
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
        composeRule.onNodeWithText("Create access").assertIsDisplayed()
    }

    @Test
    fun logoutFailureStillReturnsVerificationUserToGuestEntry() {
        val gateway = AccessAuthSessionGatewayFixture(
            payload = verificationRequiredPayload(),
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
