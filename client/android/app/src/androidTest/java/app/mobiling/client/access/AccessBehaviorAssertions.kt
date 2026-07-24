package app.mobiling.client.access

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.mobiling.client.auth.AccessAuthFeatureBridge

/**
 * Copyright (c) 2025 Oleksandr Tishchenko / Marketing America Corp.
 *
 * Shared assertions and shell setup for Android access behavior tests.
 */
internal fun ComposeContentTestRule.setAccessShell(
    gateway: AccessAuthSessionGatewayFixture,
) {
    setContent {
        MobilingAppShell(
            accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
        )
    }
}

internal fun ComposeContentTestRule.performAccessAction(label: String) {
    onAllNodesWithText(label)
        .filter(hasClickAction())
        .onFirst()
        .performClick()
}

internal fun ComposeContentTestRule.assertGuestEntryDisplayed(
    includeRegistrationAction: Boolean = false,
) {
    onNodeWithText("Guest entry").assertIsDisplayed()
    onNodeWithText("Sign in").assertIsDisplayed()
    if (includeRegistrationAction) {
        onNodeWithText("Create access").assertIsDisplayed()
    }
}

internal fun ComposeContentTestRule.assertSignInDisplayed() {
    onNodeWithText("Use your SmartResponsor access to enter the business workspace.")
        .assertIsDisplayed()
}

internal fun ComposeContentTestRule.assertVerificationRequiredDisplayed() {
    onNodeWithText("Accessing requires identity verification before this mobile session can continue.")
        .assertIsDisplayed()
}

internal fun ComposeContentTestRule.assertSecondFactorRequiredDisplayed() {
    onNodeWithText("Accessing requires an additional verification step before this mobile session can continue.")
        .assertIsDisplayed()
}
