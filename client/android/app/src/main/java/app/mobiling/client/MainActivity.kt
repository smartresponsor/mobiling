package app.mobiling.client

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import app.mobiling.client.access.MobilingAppShell
import app.mobiling.client.access.OneTaskerLaunchSplash
import app.mobiling.client.consumer.OneTaskerTheme
import app.mobiling.client.navigation.MobileRouteResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = Color.WHITE
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = true
        }
        val localText = AndroidMobileTextCatalog.current(this)

        setContent {
            var graph by remember { mutableStateOf<MobileApplicationGraph?>(null) }

            LaunchedEffect(Unit) {
                graph = withContext(Dispatchers.Default) {
                    MobileApplicationGraph.current(localText)
                }
            }

            OneTaskerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val resolvedGraph = graph
                    if (resolvedGraph == null) {
                        OneTaskerLaunchSplash(visible = true)
                    } else {
                        MobilingAppShell(
                            accessAuthFeatureBridge = resolvedGraph.accessAuthFeatureBridge,
                            cartFeatureBridge = resolvedGraph.cartFeatureBridge,
                            attachmentFeatureBridge = resolvedGraph.attachmentFeatureBridge,
                            catalogFeatureBridge = resolvedGraph.catalogFeatureBridge,
                            navigationShellGateway = resolvedGraph.navigationShellGateway,
                            messageFeatureBridge = resolvedGraph.messageFeatureBridge,
                            productGateway = resolvedGraph.productGateway,
                            orderGateway = resolvedGraph.orderGateway,
                            projectGateway = resolvedGraph.projectGateway,
                            vendorProfileGateway = resolvedGraph.vendorProfileGateway,
                            vendorSummaryGateway = resolvedGraph.vendorSummaryGateway,
                            vendorStatementGateway = resolvedGraph.vendorStatementGateway,
                            vendorPayoutGateway = resolvedGraph.vendorPayoutGateway,
                            vendorTransactionGateway = resolvedGraph.vendorTransactionGateway,
                            initialRoute = resolvedGraph.composition.configuration.initialDestination.resolvedRoute(
                                MobileRouteResolver::isCurrentlyRenderable,
                            ),
                            catalogEnabled = resolvedGraph.composition.configuration.catalog.isPrimaryCatalogEnabled(),
                            availableRetailKinds = resolvedGraph.composition.configuration.retail.availableKinds,
                            navigationLabelResolver = resolvedGraph.composition.configuration.textResolver::resolveNavigation,
                        )
                    }
                }
            }
        }
    }
}
