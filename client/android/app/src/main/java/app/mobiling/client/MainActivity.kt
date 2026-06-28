package app.mobiling.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.mobiling.client.access.MobilingAppShell
import app.mobiling.client.auth.AuthFeatureBridge
import app.mobiling.client.cart.CartFeatureBridge
import app.mobiling.client.data.auth.session.HttpAuthSessionGateway
import app.mobiling.client.data.cart.CartHttpGateway
import app.mobiling.client.data.navigation.shell.HttpNavigationShellGateway
import app.mobiling.client.data.vendor.payout.HttpVendorPayoutGateway
import app.mobiling.client.data.vendor.profile.HttpVendorProfileGateway
import app.mobiling.client.data.vendor.statement.HttpVendorStatementGateway
import app.mobiling.client.data.vendor.summary.HttpVendorSummaryGateway
import app.mobiling.client.data.vendor.transaction.HttpVendorTransactionGateway

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authFeatureBridge = AuthFeatureBridge(
            HttpAuthSessionGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
        )
        val cartGateway = CartHttpGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl)
        val cartFeatureBridge = CartFeatureBridge(
            reader = cartGateway,
            writer = cartGateway,
            checkoutGateway = cartGateway,
        )

        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    MobilingAppShell(
                        authFeatureBridge = authFeatureBridge,
                        cartFeatureBridge = cartFeatureBridge,
                        navigationShellGateway = HttpNavigationShellGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                        vendorProfileGateway = HttpVendorProfileGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                        vendorSummaryGateway = HttpVendorSummaryGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                        vendorStatementGateway = HttpVendorStatementGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                        vendorPayoutGateway = HttpVendorPayoutGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                        vendorTransactionGateway = HttpVendorTransactionGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                    )
                }
            }
        }
    }
}


