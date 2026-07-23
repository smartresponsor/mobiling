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
 * Behavioral coverage for Android registration response handling.
 */
class RegistrationBehaviorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun registrationResponseRoutesToVerificationRequired() {
        val gateway = AccessAuthSessionGatewayFixture(
            registrationPayload = testSessionPayload(requiresVerification = true),
        )

        composeRule.setContent {
            MobilingAppShell(accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway))
        }

        submitRegistration()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.registrationCalls == 1 }
        composeRule
            .onNodeWithText("Accessing requires identity verification before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun registrationResponseRoutesToSecondFactorRequired() {
        val gateway = AccessAuthSessionGatewayFixture(
            registrationPayload = testSessionPayload(requiresSecondFactor = true),
        )

        composeRule.setContent {
            MobilingAppShell(accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway))
        }

        submitRegistration()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.registrationCalls == 1 }
        composeRule
            .onNodeWithText("Accessing requires an additional verification step before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun registrationResponseRoutesToAuthenticatedContentWithVendorIdentity() {
        val gateway = AccessAuthSessionGatewayFixture(
            registrationPayload = testSessionPayload(authenticated = true),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
                authenticatedContent = { vendorId, _ -> Text("Registered vendor: $vendorId") },
            )
        }

        submitRegistration()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.registrationCalls == 1 }
        composeRule.onNodeWithText("Registered vendor: test-vendor").assertIsDisplayed()
    }

    @Test
    fun unavailableRegistrationBridgeKeepsFormAndShowsStatus() {
        composeRule.setContent { MobilingAppShell() }

        submitRegistration()
        composeRule.onNodeWithText("Access service is unavailable.").assertIsDisplayed()
        assertRegistrationFormDisplayed()
    }

    @Test
    fun registrationFailureKeepsFormAndShowsStatus() {
        val gateway = AccessAuthSessionGatewayFixture(
            registrationFailure = IllegalStateException("registration unavailable"),
        )

        composeRule.setContent {
            MobilingAppShell(accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway))
        }

        submitRegistration()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.registrationCalls == 1 }
        composeRule.onNodeWithText("Access could not be created.").assertIsDisplayed()
        assertRegistrationFormDisplayed()
    }

    private fun submitRegistration() {
        composeRule.onNodeWithText("Create access").performClick()
        composeRule.onNodeWithText("Company name").performTextInput("Test Company")
        composeRule.onNodeWithText("Email").performTextInput("user@example.com")
        composeRule.onNodeWithText("Password").performTextInput("password")
        composeRule.onNodeWithText("Create access").performClick()
    }

    private fun assertRegistrationFormDisplayed() {
        composeRule.onNodeWithText("Company name").assertIsDisplayed()
        composeRule.onNodeWithText("Email").assertIsDisplayed()
        composeRule.onNodeWithText("Password").assertIsDisplayed()
        composeRule
            .onNodeWithText("Set up a guest entry for the SmartResponsor workspace.")
            .assertIsDisplayed()
    }
}
