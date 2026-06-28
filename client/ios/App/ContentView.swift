import SwiftUI
import MobileClient

struct ContentView: View {
    var body: some View {
        MobilingAppShell(
            authFeatureBridge: AuthFeatureBridge(
                gateway: HttpAuthSessionGateway(baseUrl: MobileClientRuntimeConfig.mobileEdgeBaseUrl)
            ),
            navigationShellGateway: HttpNavigationShellGateway(baseUrl: MobileClientRuntimeConfig.mobileEdgeBaseUrl),
            vendorProfileGateway: HttpVendorProfileGateway(baseUrl: MobileClientRuntimeConfig.mobileEdgeBaseUrl),
            vendorSummaryGateway: HttpVendorSummaryGateway(baseUrl: MobileClientRuntimeConfig.mobileEdgeBaseUrl),
            vendorStatementGateway: HttpVendorStatementGateway(baseUrl: MobileClientRuntimeConfig.mobileEdgeBaseUrl),
            vendorPayoutGateway: HttpVendorPayoutGateway(baseUrl: MobileClientRuntimeConfig.mobileEdgeBaseUrl),
            vendorTransactionGateway: HttpVendorTransactionGateway(baseUrl: MobileClientRuntimeConfig.mobileEdgeBaseUrl)
        )
    }
}


