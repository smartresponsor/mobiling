package app.mobiling.client

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import app.mobiling.client.access.MobilingAppShell
import app.mobiling.client.attachment.AttachmentFeatureBridge
import app.mobiling.client.auth.AccessAuthFeatureBridge
import app.mobiling.client.cart.CartFeatureBridge
import app.mobiling.client.data.attachment.AttachmentHttpGateway
import app.mobiling.client.data.auth.session.AccessHttpAuthSessionGateway
import app.mobiling.client.data.cart.CartHttpGateway
import app.mobiling.client.data.navigation.shell.NavigationHttpShellGateway
import app.mobiling.client.data.order.OrderHttpGateway
import app.mobiling.client.data.product.ProductHttpGateway
import app.mobiling.client.data.project.ProjectHttpGateway
import app.mobiling.client.data.vendor.payout.VendorHttpPayoutGateway
import app.mobiling.client.data.vendor.profile.VendorHttpProfileGateway
import app.mobiling.client.data.vendor.statement.VendorHttpStatementGateway
import app.mobiling.client.data.vendor.summary.VendorHttpSummaryGateway
import app.mobiling.client.data.vendor.transaction.VendorHttpTransactionGateway
import app.mobiling.client.consumer.OneTaskerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = Color.WHITE
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = true
        }
        val accessAuthFeatureBridge = AccessAuthFeatureBridge(
            AccessHttpAuthSessionGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
        )
        val cartGateway = CartHttpGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl)
        val cartFeatureBridge = CartFeatureBridge(
            reader = cartGateway,
            writer = cartGateway,
            checkoutGateway = cartGateway,
        )
        val attachmentGateway = AttachmentHttpGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl)
        val attachmentFeatureBridge = AttachmentFeatureBridge(
            reader = attachmentGateway,
            writer = attachmentGateway,
        )

        setContent {
            OneTaskerTheme {
                Surface(Modifier.fillMaxSize()) {
                    MobilingAppShell(
                        accessAuthFeatureBridge = accessAuthFeatureBridge,
                        cartFeatureBridge = cartFeatureBridge,
                        attachmentFeatureBridge = attachmentFeatureBridge,
                        navigationShellGateway = NavigationHttpShellGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                        productGateway = ProductHttpGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                        orderGateway = OrderHttpGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                        projectGateway = ProjectHttpGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                        vendorProfileGateway = VendorHttpProfileGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                        vendorSummaryGateway = VendorHttpSummaryGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                        vendorStatementGateway = VendorHttpStatementGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                        vendorPayoutGateway = VendorHttpPayoutGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                        vendorTransactionGateway = VendorHttpTransactionGateway(baseUrl = MobileClientRuntimeConfig.mobileEdgeBaseUrl),
                    )
                }
            }
        }
    }
}


