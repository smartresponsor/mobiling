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
 * Behavioral coverage for Android registration response handling.
 */
class RegistrationBehaviorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun registrationResponseRoutesToVerificationRequired() {
        val gateway = RegistrationGateway(
            registrationPayload = session(requiresVerification = true),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
            )
        }

        submitRegistration()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.registrationCalls == 1 }
        composeRule
            .onNodeWithText("Accessing requires identity verification before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun registrationResponseRoutesToSecondFactorRequired() {
        val gateway = RegistrationGateway(
            registrationPayload = session(requiresSecondFactor = true),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
            )
        }

        submitRegistration()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.registrationCalls == 1 }
        composeRule
            .onNodeWithText("Accessing requires an additional verification step before this mobile session can continue.")
            .assertIsDisplayed()
    }

    @Test
    fun registrationResponseRoutesToAuthenticatedContentWithVendorIdentity() {
        val gateway = RegistrationGateway(
            registrationPayload = session(authenticated = true),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
                authenticatedContent = { vendorId, _ ->
                    Text("Registered vendor: $vendorId")
                },
            )
        }

        submitRegistration()
        composeRule.waitUntil(timeoutMillis = 5_000) { gateway.registrationCalls == 1 }
        composeRule.onNodeWithText("Registered vendor: test-vendor").assertIsDisplayed()
    }

    @Test
    fun unavailableRegistrationBridgeKeepsFormAndShowsStatus() {
        composeRule.setContent {
            MobilingAppShell()
        }

        submitRegistration()
        composeRule.onNodeWithText("Access service is unavailable.").assertIsDisplayed()
        assertRegistrationFormDisplayed()
    }

    @Test
    fun registrationFailureKeepsFormAndShowsStatus() {
        val gateway = RegistrationGateway(
            registrationFailure = IllegalStateException("registration unavailable"),
        )

        composeRule.setContent {
            MobilingAppShell(
                accessAuthFeatureBridge = AccessAuthFeatureBridge(gateway),
            )
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

private class RegistrationGateway(
    private val registrationPayload: AccessAuthSessionPayload = AccessAuthSessionPayload(
        status = "guest",
        sessionId = null,
        vendorId = null,
        authenticated = false,
        requiresVerification = false,
        requiresSecondFactor = false,
    ),
    private val registrationFailure: RuntimeException? = null,
) : AccessAuthSessionGateway {
    var registrationCalls: Int = 0
        private set

    override suspend fun startAuth(request: AccessStartAuthRequest): AccessAuthSessionPayload = registrationPayload

    override suspend fun registerAuth(request: AccessRegisterAuthRequest): AccessAuthSessionPayload {
        registrationCalls += 1
        registrationFailure?.let { throw it }

        return registrationPayload
    }

    override suspend fun restoreAuth(): AccessAuthSessionPayload = AccessAuthSessionPayload(
        status = "guest",
        sessionId = null,
        vendorId = null,
        authenticated = false,
        requiresVerification = false,
        requiresSecondFactor = false,
    )

    override suspend fun logoutAuth() = Unit

    override suspend fun resendVerification(): AccessAuthSessionPayload = registrationPayload

    override suspend fun confirmVerification(
        request: AccessConfirmVerificationRequest,
    ): AccessAuthSessionPayload = registrationPayload

    override suspend fun challengeSecondFactor(): AccessAuthSessionPayload = registrationPayload

    override suspend fun verifySecondFactor(
        request: AccessVerifySecondFactorRequest,
    ): AccessAuthSessionPayload = registrationPayload

    override suspend fun requestRecovery(
        request: AccessRequestRecoveryRequest,
    ): AccessAuthSessionPayload = registrationPayload

    override suspend fun resetRecovery(
        request: AccessResetRecoveryRequest,
    ): AccessAuthSessionPayload = registrationPayload
}
