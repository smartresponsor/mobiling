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
        let attachmentGateway = HttpAttachmentGateway(baseUrl: baseUrl)
        let cartGateway = CartHttpGateway(baseUrl: baseUrl)
        let catalogGateway = CatalogHttpGateway(
            baseUrl: baseUrl,
            catalogCode: composition.configuration.catalog.primaryCatalog
        )

        return MobileApplicationGraph(
            composition: composition,
            authFeatureBridge: AuthFeatureBridge(gateway: HttpAuthSessionGateway(baseUrl: baseUrl)),
            attachmentFeatureBridge: AttachmentFeatureBridge(reader: attachmentGateway, writer: attachmentGateway),
            cartFeatureBridge: CartFeatureBridge(reader: cartGateway, writer: cartGateway, checkoutGateway: cartGateway),
            catalogFeatureBridge: CatalogFeatureBridge(browseGateway: catalogGateway, detailGateway: catalogGateway),
            navigationShellGateway: HttpNavigationShellGateway(baseUrl: baseUrl),
            messageFeatureBridge: MessageFeatureBridge(gateway: MessageHttpThreadGateway(baseUrl: baseUrl)),
            notificationFeatureBridge: NotificationFeatureBridge(gateway: NotificationHttpGateway(baseUrl: baseUrl)),
            supportFeatureBridge: SupportFeatureBridge(gateway: SupportHttpGateway(baseUrl: baseUrl)),
            vendorProfileGateway: HttpVendorProfileGateway(baseUrl: baseUrl),
            vendorSummaryGateway: HttpVendorSummaryGateway(baseUrl: baseUrl),
            vendorStatementGateway: HttpVendorStatementGateway(baseUrl: baseUrl),
            vendorPayoutGateway: HttpVendorPayoutGateway(baseUrl: baseUrl),
            vendorTransactionGateway: HttpVendorTransactionGateway(baseUrl: baseUrl),
            vendorCrudGateway: HttpVendorCrudGateway(baseUrl: baseUrl),
            retailPlacementGateway: HttpRetailPlacementGateway(baseUrl: baseUrl),
            walletGateway: WalletHttpGateway(baseUrl: baseUrl)
        )
    }
}
