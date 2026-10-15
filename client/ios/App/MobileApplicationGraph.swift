import Foundation
import MobileClient

struct MobileApplicationGraph {
    let composition: MobileApplicationComposition
    let authFeatureBridge: AuthFeatureBridge
    let attachmentFeatureBridge: AttachmentFeatureBridge
    let cartFeatureBridge: CartFeatureBridge
    let catalogFeatureBridge: CatalogFeatureBridge
    let navigationShellGateway: NavigationShellGateway
    let messageFeatureBridge: MessageFeatureBridge
    let notificationFeatureBridge: NotificationFeatureBridge
    let supportFeatureBridge: SupportFeatureBridge
    let vendorProfileGateway: VendorProfileGateway
    let vendorSummaryGateway: VendorSummaryGateway
    let vendorStatementGateway: VendorStatementGateway
    let vendorPayoutGateway: VendorPayoutGateway
    let vendorTransactionGateway: VendorTransactionGateway
    let vendorCrudGateway: VendorCrudGateway
    let retailPlacementGateway: RetailPlacementGateway
    let walletGateway: WalletGateway

    static func current(composition: MobileApplicationComposition = .current) -> MobileApplicationGraph {
        let baseUrl = composition.mobileEdgeBaseUrl
        let sessionConfiguration = URLSessionConfiguration.default
        sessionConfiguration.httpAdditionalHeaders = [
            "X-Application-Key": composition.configuration.product.code,
            "X-Application-Environment": composition.configuration.environment.code,
        ]
        let session = URLSession(configuration: sessionConfiguration)
        let attachmentGateway = HttpAttachmentGateway(baseUrl: baseUrl, session: session)
        let cartGateway = CartHttpGateway(baseUrl: baseUrl, session: session)
        let catalogGateway = CatalogHttpGateway(
            baseUrl: baseUrl,
            catalogCode: composition.configuration.catalog.primaryCatalog,
            session: session
        )

        return MobileApplicationGraph(
            composition: composition,
            authFeatureBridge: AuthFeatureBridge(gateway: HttpAuthSessionGateway(baseUrl: baseUrl, session: session)),
            attachmentFeatureBridge: AttachmentFeatureBridge(reader: attachmentGateway, writer: attachmentGateway),
            cartFeatureBridge: CartFeatureBridge(reader: cartGateway, writer: cartGateway, checkoutGateway: cartGateway),
            catalogFeatureBridge: CatalogFeatureBridge(browseGateway: catalogGateway, detailGateway: catalogGateway),
            navigationShellGateway: HttpNavigationShellGateway(baseUrl: baseUrl, session: session),
            messageFeatureBridge: MessageFeatureBridge(gateway: MessageHttpThreadGateway(baseUrl: baseUrl, session: session)),
            notificationFeatureBridge: NotificationFeatureBridge(gateway: NotificationHttpGateway(baseUrl: baseUrl, session: session)),
            supportFeatureBridge: SupportFeatureBridge(gateway: SupportHttpGateway(baseUrl: baseUrl, session: session)),
            vendorProfileGateway: HttpVendorProfileGateway(baseUrl: baseUrl, session: session),
            vendorSummaryGateway: HttpVendorSummaryGateway(baseUrl: baseUrl, session: session),
            vendorStatementGateway: HttpVendorStatementGateway(baseUrl: baseUrl, session: session),
            vendorPayoutGateway: HttpVendorPayoutGateway(baseUrl: baseUrl, session: session),
            vendorTransactionGateway: HttpVendorTransactionGateway(baseUrl: baseUrl, session: session),
            vendorCrudGateway: HttpVendorCrudGateway(baseUrl: baseUrl, session: session),
            retailPlacementGateway: HttpRetailPlacementGateway(baseUrl: baseUrl, session: session),
            walletGateway: WalletHttpGateway(baseUrl: baseUrl, session: session)
        )
    }
}
