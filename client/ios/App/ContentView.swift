import SwiftUI
import MobileClient

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
            vendorCrudGateway: graph.vendorCrudGateway
        )
    }
}
