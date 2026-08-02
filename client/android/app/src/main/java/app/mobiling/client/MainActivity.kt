package app.mobiling.client

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import app.mobiling.client.access.MobilingAppShell
import app.mobiling.client.consumer.OneTaskerTheme
import app.mobiling.client.navigation.MobileRouteResolver

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = Color.WHITE
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = true
        }
        val graph = MobileApplicationGraph.current(AndroidMobileTextCatalog.current(this))

        setContent {
            OneTaskerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MobilingAppShell(
                        accessAuthFeatureBridge = graph.accessAuthFeatureBridge,
                        cartFeatureBridge = graph.cartFeatureBridge,
                        attachmentFeatureBridge = graph.attachmentFeatureBridge,
                        catalogFeatureBridge = graph.catalogFeatureBridge,
                        navigationShellGateway = graph.navigationShellGateway,
                        productGateway = graph.productGateway,
                        orderGateway = graph.orderGateway,
                        projectGateway = graph.projectGateway,
                        vendorProfileGateway = graph.vendorProfileGateway,
                        vendorSummaryGateway = graph.vendorSummaryGateway,
                        vendorStatementGateway = graph.vendorStatementGateway,
                        vendorPayoutGateway = graph.vendorPayoutGateway,
                        vendorTransactionGateway = graph.vendorTransactionGateway,
                        initialRoute = graph.composition.configuration.initialDestination.resolvedRoute(
                            MobileRouteResolver::isCurrentlyRenderable,
                        ),
                        catalogEnabled = graph.composition.configuration.catalog.isPrimaryCatalogEnabled(),
                        availableRetailKinds = graph.composition.configuration.retail.availableKinds,
                        navigationLabelResolver = graph.composition.configuration.textResolver::resolveNavigation,
                    )
                }
            }
        }
    }
}
