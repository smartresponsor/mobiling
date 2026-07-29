import SwiftUI
import MobileClient

struct ContentView: View {
    private let composition = MobileApplicationComposition.current

    var body: some View {
        MobilingAppShell(
            authFeatureBridge: AuthFeatureBridge(
                gateway: HttpAuthSessionGateway(baseUrl: composition.mobileEdgeBaseUrl)
            ),
            attachmentFeatureBridge: AttachmentFeatureBridge(
                reader: HttpAttachmentGateway(baseUrl: composition.mobileEdgeBaseUrl),
                writer: HttpAttachmentGateway(baseUrl: composition.mobileEdgeBaseUrl)
            ),
            navigationShellGateway: HttpNavigationShellGateway(baseUrl: composition.mobileEdgeBaseUrl),
            vendorProfileGateway: HttpVendorProfileGateway(baseUrl: composition.mobileEdgeBaseUrl),
            vendorSummaryGateway: HttpVendorSummaryGateway(baseUrl: composition.mobileEdgeBaseUrl),
            vendorStatementGateway: HttpVendorStatementGateway(baseUrl: composition.mobileEdgeBaseUrl),
            vendorPayoutGateway: HttpVendorPayoutGateway(baseUrl: composition.mobileEdgeBaseUrl),
            vendorTransactionGateway: HttpVendorTransactionGateway(baseUrl: composition.mobileEdgeBaseUrl),
            vendorCrudGateway: HttpVendorCrudGateway(baseUrl: composition.mobileEdgeBaseUrl)
        )
    }
}


