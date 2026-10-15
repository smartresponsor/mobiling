import SwiftUI
import Combine
import MobileClient
import CoreConfig

struct ContentView: View {
    private let graph = MobileApplicationGraph.current()
    private let pushTokenLifecycle = PushTokenLifecycle()

    var body: some View {
        MobilingAppShell(
            authFeatureBridge: graph.authFeatureBridge,
            attachmentFeatureBridge: graph.attachmentFeatureBridge,
            cartFeatureBridge: graph.cartFeatureBridge,
            catalogFeatureBridge: graph.catalogFeatureBridge,
            navigationShellGateway: graph.navigationShellGateway,
            messageFeatureBridge: graph.messageFeatureBridge,
            notificationFeatureBridge: graph.notificationFeatureBridge,
            supportFeatureBridge: graph.supportFeatureBridge,
            vendorProfileGateway: graph.vendorProfileGateway,
            vendorSummaryGateway: graph.vendorSummaryGateway,
            vendorStatementGateway: graph.vendorStatementGateway,
            vendorPayoutGateway: graph.vendorPayoutGateway,
            vendorTransactionGateway: graph.vendorTransactionGateway,
            vendorCrudGateway: graph.vendorCrudGateway,
            retailPlacementGateway: graph.retailPlacementGateway,
            walletGateway: graph.walletGateway,
            initialRoute: graph.composition.configuration.initialDestination.resolvedRoute(
                isRenderable: MobileRouteResolver.isCurrentlyRenderable
            ),
            publicInitialRoute: graph.composition.configuration.publicInitialDestination.resolvedRoute {
                ["home", "catalog", "users", "orders", "cart", "sign-in"].contains($0)
            },
            catalogEnabled: graph.composition.configuration.catalog.isPrimaryCatalogEnabled,
            availableRetailKinds: graph.composition.configuration.retail.availableKinds,
            navigationLabelResolver: graph.composition.configuration.textResolver.resolveNavigation,
            onAuthenticated: {
                _ = await pushTokenLifecycle.sync(
                    bridge: graph.notificationFeatureBridge,
                    appKey: graph.composition.configuration.product.code
                )
            },
            onBeforeSignOut: {
                _ = await pushTokenLifecycle.disable(
                    bridge: graph.notificationFeatureBridge,
                    appKey: graph.composition.configuration.product.code
                )
            }
        )
        .onReceive(NotificationCenter.default.publisher(for: PushTokenLifecycle.tokenDidChangeNotification)) { _ in
            Task {
                _ = await pushTokenLifecycle.sync(
                    bridge: graph.notificationFeatureBridge,
                    appKey: graph.composition.configuration.product.code
                )
            }
        }
    }
}
