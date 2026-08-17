import SwiftUI
import MobileClient
import CoreConfig

struct ContentView: View {
    private let graph = MobileApplicationGraph.current()

    var body: some View {
        MobilingAppShell(
            authFeatureBridge: graph.authFeatureBridge,
            attachmentFeatureBridge: graph.attachmentFeatureBridge,
            navigationShellGateway: graph.navigationShellGateway,
            messageFeatureBridge: graph.messageFeatureBridge,
            vendorProfileGateway: graph.vendorProfileGateway,
            vendorSummaryGateway: graph.vendorSummaryGateway,
            vendorStatementGateway: graph.vendorStatementGateway,
            vendorPayoutGateway: graph.vendorPayoutGateway,
            vendorTransactionGateway: graph.vendorTransactionGateway,
            vendorCrudGateway: graph.vendorCrudGateway,
            initialRoute: graph.composition.configuration.initialDestination.resolvedRoute(
                isRenderable: MobileRouteResolver.isCurrentlyRenderable
            ),
            publicInitialRoute: graph.composition.configuration.publicInitialDestination.resolvedRoute {
                ["home", "catalog", "users", "orders", "cart", "sign-in"].contains($0)
            },
            catalogEnabled: graph.composition.configuration.catalog.isPrimaryCatalogEnabled,
            availableRetailKinds: graph.composition.configuration.retail.availableKinds,
            navigationLabelResolver: graph.composition.configuration.textResolver.resolveNavigation
        )
    }
}
