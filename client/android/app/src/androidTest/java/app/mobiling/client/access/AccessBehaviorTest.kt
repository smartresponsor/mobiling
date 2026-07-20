package app.mobiling.client.access

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
}
