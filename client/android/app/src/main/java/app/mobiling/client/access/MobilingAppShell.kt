package app.mobiling.client.access

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.mobiling.client.contract.auth.session.AuthSessionPayload
import app.mobiling.client.auth.AuthFeatureBridge
import app.mobiling.client.dashboard.MobileDashboardShell
import app.mobiling.client.data.navigation.shell.NavigationShellGateway
import app.mobiling.client.data.vendor.payout.VendorPayoutGateway
import app.mobiling.client.data.vendor.profile.VendorProfileGateway
import app.mobiling.client.data.vendor.statement.VendorStatementGateway
import app.mobiling.client.data.vendor.summary.VendorSummaryGateway
import kotlinx.coroutines.launch

@Composable
fun MobilingAppShell(
    authFeatureBridge: AuthFeatureBridge? = null,
    navigationShellGateway: NavigationShellGateway? = null,
    vendorProfileGateway: VendorProfileGateway? = null,
    vendorSummaryGateway: VendorSummaryGateway? = null,
    vendorStatementGateway: VendorStatementGateway? = null,
    vendorPayoutGateway: VendorPayoutGateway? = null,
) {
    var currentScreen by rememberSaveable { mutableStateOf(AccessScreen.Welcome) }
    var activeVendorId by rememberSaveable { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun applyAccessSession(payload: AuthSessionPayload) {
        activeVendorId = payload.vendorId
        currentScreen = payload.toAccessScreen()
    }

    fun clearAccessSession() {
        coroutineScope.launch {
            try {
                authFeatureBridge?.logout()
            } catch (_: Exception) {
            }

            activeVendorId = null
            currentScreen = AccessScreen.Welcome
        }
    }

    LaunchedEffect(authFeatureBridge) {
        val payload = try {
            authFeatureBridge?.restore()
        } catch (_: Exception) {
            null
        }

        if (payload != null) {
            applyAccessSession(payload)
        }
    }

    Surface(Modifier.fillMaxSize()) {
        when (currentScreen) {
            AccessScreen.Dashboard -> MobileDashboardShell(
                navigationShellGateway = navigationShellGateway,
                vendorId = activeVendorId,
                vendorProfileGateway = vendorProfileGateway,
                vendorSummaryGateway = vendorSummaryGateway,
                vendorStatementGateway = vendorStatementGateway,
                vendorPayoutGateway = vendorPayoutGateway,
                onSignOut = { clearAccessSession() },
            )

            AccessScreen.Welcome -> AccessWelcomeScreen(
                onSignIn = { currentScreen = AccessScreen.SignIn },
                onCreateAccess = { currentScreen = AccessScreen.Register },
            )

            AccessScreen.SignIn -> SignInScreen(
                onBack = { currentScreen = AccessScreen.Welcome },
                onCreateAccess = { currentScreen = AccessScreen.Register },
                onRecoverAccess = { currentScreen = AccessScreen.RecoveryRequest },
                onStartAccess = { request -> authFeatureBridge?.start(request) },
                onAccessSession = { payload -> applyAccessSession(payload) },
            )

            AccessScreen.Register -> RegisterAccessScreen(
                onBack = { currentScreen = AccessScreen.Welcome },
                onSignIn = { currentScreen = AccessScreen.SignIn },
                onRegisterAccess = { request -> authFeatureBridge?.register(request) },
                onAccessSession = { payload -> applyAccessSession(payload) },
            )

            AccessScreen.VerificationRequired -> VerificationRequiredScreen(
                onBack = { currentScreen = AccessScreen.SignIn },
                onUseDifferentAccess = { clearAccessSession() },
            )

            AccessScreen.SecondFactorRequired -> SecondFactorRequiredScreen(
                onBack = { currentScreen = AccessScreen.SignIn },
                onUseDifferentAccess = { clearAccessSession() },
            )

            AccessScreen.RecoveryRequest -> RecoveryRequestScreen(
                onBack = { currentScreen = AccessScreen.SignIn },
                onHaveRecoveryCode = { currentScreen = AccessScreen.RecoveryReset },
                onRequestRecovery = { request -> authFeatureBridge?.requestRecovery(request) },
                onAccessSession = { payload -> applyAccessSession(payload) },
            )

            AccessScreen.RecoveryReset -> RecoveryResetScreen(
                onBack = { currentScreen = AccessScreen.RecoveryRequest },
                onRequestRecovery = { currentScreen = AccessScreen.RecoveryRequest },
                onResetRecovery = { request -> authFeatureBridge?.resetRecovery(request) },
                onAccessSession = { payload -> applyAccessSession(payload) },
            )
        }
    }
}

