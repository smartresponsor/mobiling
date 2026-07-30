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
            vendorProfileGateway: graph.vendorProfileGateway,
            vendorSummaryGateway: graph.vendorSummaryGateway,
            vendorStatementGateway: graph.vendorStatementGateway,
            vendorPayoutGateway: graph.vendorPayoutGateway,
            vendorTransactionGateway: graph.vendorTransactionGateway,
            vendorCrudGateway: graph.vendorCrudGateway,
            initialRoute: graph.composition.configuration.initialDestination.resolvedRoute(
                isRenderable: MobileRouteResolver.isCurrentlyRenderable
            ),
            catalogEnabled: graph.composition.configuration.catalog.isPrimaryCatalogEnabled
        )
    }
}
