package app.mobiling.client

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import app.mobiling.client.access.MobilingAppShell
import app.mobiling.client.access.OneTaskerLaunchSplash
import app.mobiling.client.access.oneTaskerMotionEnabled
import app.mobiling.client.consumer.MobileBrandTheme
import app.mobiling.client.navigation.MobileRouteResolver
import app.mobiling.client.notification.AndroidPushTokenLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
            var launchSplashMounted by remember { mutableStateOf(true) }
            var launchSplashVisible by remember { mutableStateOf(true) }
            val context = LocalContext.current
            val motionEnabled = remember(context) { oneTaskerMotionEnabled(context) }
            val pushTokenLifecycle = remember(context) { AndroidPushTokenLifecycle(context.applicationContext) }

            LaunchedEffect(Unit) {
                graph = withContext(Dispatchers.Default) {
                    MobileApplicationGraph.current(localText)
                }
            }

            LaunchedEffect(graph, pushTokenLifecycle) {
                val resolvedGraph = graph ?: return@LaunchedEffect
                AndroidPushTokenLifecycle.tokenChanges.collect {
                    pushTokenLifecycle.sync(
                        resolvedGraph.notificationFeatureBridge,
                        resolvedGraph.composition.configuration.product.code,
                    )
                }
            }

            LaunchedEffect(motionEnabled) {
                delay(if (motionEnabled) 7200 else 520)
                launchSplashVisible = false
                delay(if (motionEnabled) 320 else 120)
                launchSplashMounted = false
            }

            MobileBrandTheme(BuildConfig.BRAND_PROFILE) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Box(Modifier.fillMaxSize()) {
                        val resolvedGraph = graph
                        if (resolvedGraph != null) {
                            MobilingAppShell(
                                accessAuthFeatureBridge = resolvedGraph.accessAuthFeatureBridge,
                                cartFeatureBridge = resolvedGraph.cartFeatureBridge,
                                attachmentFeatureBridge = resolvedGraph.attachmentFeatureBridge,
                                catalogFeatureBridge = resolvedGraph.catalogFeatureBridge,
                                navigationShellGateway = resolvedGraph.navigationShellGateway,
                                messageFeatureBridge = resolvedGraph.messageFeatureBridge,
                                notificationFeatureBridge = resolvedGraph.notificationFeatureBridge,
                                productGateway = resolvedGraph.productGateway,
                                orderGateway = resolvedGraph.orderGateway,
                                projectGateway = resolvedGraph.projectGateway,
                                vendorProfileGateway = resolvedGraph.vendorProfileGateway,
                                vendorSummaryGateway = resolvedGraph.vendorSummaryGateway,
                                vendorStatementGateway = resolvedGraph.vendorStatementGateway,
                                vendorPayoutGateway = resolvedGraph.vendorPayoutGateway,
                                vendorTransactionGateway = resolvedGraph.vendorTransactionGateway,
                                walletGateway = resolvedGraph.walletGateway,
                                initialRoute = resolvedGraph.composition.configuration.initialDestination.resolvedRoute(
                                    MobileRouteResolver::isCurrentlyRenderable,
                                ),
                                publicInitialRoute = resolvedGraph.composition.configuration.publicInitialDestination.resolvedRoute {
                                    it in setOf("home", "catalog", "users", "orders", "cart", "sign-in")
                                },
                                catalogEnabled = resolvedGraph.composition.configuration.catalog.isPrimaryCatalogEnabled(),
                                availableRetailKinds = resolvedGraph.composition.configuration.retail.availableKinds,
                                navigationLabelResolver = resolvedGraph.composition.configuration.textResolver::resolveNavigation,
                                onAuthenticated = {
                                    pushTokenLifecycle.sync(
                                        resolvedGraph.notificationFeatureBridge,
                                        resolvedGraph.composition.configuration.product.code,
                                    )
                                },
                                onBeforeSignOut = {
                                    pushTokenLifecycle.disable(
                                        resolvedGraph.notificationFeatureBridge,
                                        resolvedGraph.composition.configuration.product.code,
                                    )
                                },
                            )
                        }

                        if (launchSplashMounted) {
                            OneTaskerLaunchSplash(visible = launchSplashVisible)
                        }
                    }
                }
            }
        }
    }
}
