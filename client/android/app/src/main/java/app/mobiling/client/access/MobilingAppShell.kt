package app.mobiling.client.access

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.mobiling.client.attachment.AttachmentFeatureBridge
import app.mobiling.client.auth.AccessAuthFeatureBridge
import app.mobiling.client.catalog.CatalogFeatureBridge
import app.mobiling.client.contract.auth.session.AccessAuthSessionPayload
import app.mobiling.client.cart.CartFeatureBridge
import app.mobiling.client.RetailKind
import app.mobiling.client.dashboard.DashboardMobileShell
import app.mobiling.client.data.navigation.shell.NavigationShellGateway
import app.mobiling.client.message.MessageFeatureBridge
import app.mobiling.client.notification.NotificationFeatureBridge
import app.mobiling.client.support.SupportFeatureBridge
import app.mobiling.client.data.order.OrderGateway
import app.mobiling.client.data.product.ProductGateway
import app.mobiling.client.data.project.ProjectGateway
import app.mobiling.client.data.retail.placement.RetailPlacementGateway
import app.mobiling.client.data.vendor.payout.VendorPayoutGateway
import app.mobiling.client.data.vendor.profile.VendorProfileGateway
import app.mobiling.client.data.vendor.statement.VendorStatementGateway
import app.mobiling.client.data.vendor.summary.VendorSummaryGateway
import app.mobiling.client.data.vendor.transaction.VendorTransactionGateway
import app.mobiling.client.data.wallet.WalletGateway
import kotlinx.coroutines.launch

@Composable
fun MobilingAppShell(
    accessAuthFeatureBridge: AccessAuthFeatureBridge? = null,
    cartFeatureBridge: CartFeatureBridge? = null,
    attachmentFeatureBridge: AttachmentFeatureBridge? = null,
    catalogFeatureBridge: CatalogFeatureBridge? = null,
    navigationShellGateway: NavigationShellGateway? = null,
    messageFeatureBridge: MessageFeatureBridge? = null,
    notificationFeatureBridge: NotificationFeatureBridge? = null,
    supportFeatureBridge: SupportFeatureBridge? = null,
    productGateway: ProductGateway? = null,
    retailPlacementGateway: RetailPlacementGateway? = null,
    orderGateway: OrderGateway? = null,
    projectGateway: ProjectGateway? = null,
    vendorProfileGateway: VendorProfileGateway? = null,
    vendorSummaryGateway: VendorSummaryGateway? = null,
    vendorStatementGateway: VendorStatementGateway? = null,
    vendorPayoutGateway: VendorPayoutGateway? = null,
    vendorTransactionGateway: VendorTransactionGateway? = null,
    walletGateway: WalletGateway? = null,
    initialRoute: String = "dashboard",
    publicInitialRoute: String = "home",
    catalogEnabled: Boolean = true,
    availableRetailKinds: List<RetailKind> = RetailKind.entries,
    navigationLabelResolver: (route: String?, key: String, backendLabel: String) -> String = { _, _, label -> label },
    onAuthenticated: suspend () -> Unit = {},
    onBeforeSignOut: suspend () -> Unit = {},
    authenticatedContent: (@Composable (vendorId: String?, onSignOut: () -> Unit) -> Unit)? = null,
) {
    var currentScreen by remember(publicInitialRoute) {
        mutableStateOf(if (publicInitialRoute == "sign-in") AccessScreen.SignIn else AccessScreen.Welcome)
    }
    var activeVendorId by remember { mutableStateOf<String?>(null) }
    var activeUserUuid by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun applyAccessSession(payload: AccessAuthSessionPayload) {
        activeVendorId = payload.vendorId
        activeUserUuid = payload.userUuid
        currentScreen = payload.toAccessScreen()
        if (currentScreen == AccessScreen.Dashboard) {
            coroutineScope.launch { onAuthenticated() }
        }
    }

    fun clearAccessSession() {
        coroutineScope.launch {
            try {
                onBeforeSignOut()
            } catch (_: Exception) {
            }

            try {
                accessAuthFeatureBridge?.logout()
            } catch (_: Exception) {
            }

            activeVendorId = null
            activeUserUuid = null
            currentScreen = if (publicInitialRoute == "sign-in") AccessScreen.SignIn else AccessScreen.Welcome
        }
    }

    LaunchedEffect(accessAuthFeatureBridge) {
        val payload = try {
            accessAuthFeatureBridge?.restore()
        } catch (_: Exception) {
            null
        }

        if (payload != null) {
            applyAccessSession(payload)
        }
    }

    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            when (currentScreen) {
            AccessScreen.Dashboard -> {
                if (authenticatedContent != null) {
                    authenticatedContent(activeVendorId) { clearAccessSession() }
                } else {
                    DashboardMobileShell(
                        navigationShellGateway = navigationShellGateway,
                        messageFeatureBridge = messageFeatureBridge,
                        notificationFeatureBridge = notificationFeatureBridge,
                        supportFeatureBridge = supportFeatureBridge,
                        productGateway = productGateway,
                        retailPlacementGateway = retailPlacementGateway,
                        orderGateway = orderGateway,
                        projectGateway = projectGateway,
                        cartFeatureBridge = cartFeatureBridge,
                        catalogFeatureBridge = catalogFeatureBridge,
                        attachmentFeatureBridge = attachmentFeatureBridge,
                        vendorId = activeVendorId,
                        userUuid = activeUserUuid,
                        vendorProfileGateway = vendorProfileGateway,
                        vendorSummaryGateway = vendorSummaryGateway,
                        vendorStatementGateway = vendorStatementGateway,
                        vendorPayoutGateway = vendorPayoutGateway,
                        vendorTransactionGateway = vendorTransactionGateway,
                        walletGateway = walletGateway,
                        initialRoute = initialRoute,
                        catalogEnabled = catalogEnabled,
                        availableRetailKinds = availableRetailKinds,
                        navigationLabelResolver = navigationLabelResolver,
                        onSignOut = { clearAccessSession() },
                    )
                }
            }

            AccessScreen.Welcome -> AccessWelcomeScreen(
                initialRoute = publicInitialRoute,
                catalogFeatureBridge = catalogFeatureBridge,
                cartFeatureBridge = cartFeatureBridge,
                onSignIn = { currentScreen = AccessScreen.SignIn },
                onCreateAccess = { currentScreen = AccessScreen.Register },
            )

            AccessScreen.SignIn -> SignInScreen(
                onBack = { currentScreen = AccessScreen.Welcome },
                onCreateAccess = { currentScreen = AccessScreen.Register },
                onRecoverAccess = { currentScreen = AccessScreen.RecoveryRequest },
                onStartAccess = { request -> accessAuthFeatureBridge?.start(request) },
                onAccessSession = { payload -> applyAccessSession(payload) },
            )

            AccessScreen.Register -> RegisterAccessScreen(
                onBack = { currentScreen = AccessScreen.Welcome },
                onSignIn = { currentScreen = AccessScreen.SignIn },
                onRegisterAccess = { request -> accessAuthFeatureBridge?.register(request) },
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
                onRequestRecovery = { request -> accessAuthFeatureBridge?.requestRecovery(request) },
                onAccessSession = { payload -> applyAccessSession(payload) },
            )

            AccessScreen.RecoveryReset -> RecoveryResetScreen(
                onBack = { currentScreen = AccessScreen.RecoveryRequest },
                onRequestRecovery = { currentScreen = AccessScreen.RecoveryRequest },
                onResetRecovery = { request -> accessAuthFeatureBridge?.resetRecovery(request) },
                onAccessSession = { payload -> applyAccessSession(payload) },
            )
            }
        }
    }
}
