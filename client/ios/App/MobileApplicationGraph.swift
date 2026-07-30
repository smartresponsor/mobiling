import MobileClient

struct MobileApplicationGraph {
    let composition: MobileApplicationComposition
    let authFeatureBridge: AuthFeatureBridge
    let attachmentFeatureBridge: AttachmentFeatureBridge
    let navigationShellGateway: NavigationShellGateway
    let vendorProfileGateway: VendorProfileGateway
    let vendorSummaryGateway: VendorSummaryGateway
    let vendorStatementGateway: VendorStatementGateway
    let vendorPayoutGateway: VendorPayoutGateway
    let vendorTransactionGateway: VendorTransactionGateway
    let vendorCrudGateway: VendorCrudGateway

    static func current(composition: MobileApplicationComposition = .current) -> MobileApplicationGraph {
        let baseUrl = composition.mobileEdgeBaseUrl
        let attachmentGateway = HttpAttachmentGateway(baseUrl: baseUrl)

        return MobileApplicationGraph(
            composition: composition,
            authFeatureBridge: AuthFeatureBridge(gateway: HttpAuthSessionGateway(baseUrl: baseUrl)),
            attachmentFeatureBridge: AttachmentFeatureBridge(reader: attachmentGateway, writer: attachmentGateway),
            navigationShellGateway: HttpNavigationShellGateway(baseUrl: baseUrl),
            vendorProfileGateway: HttpVendorProfileGateway(baseUrl: baseUrl),
            vendorSummaryGateway: HttpVendorSummaryGateway(baseUrl: baseUrl),
            vendorStatementGateway: HttpVendorStatementGateway(baseUrl: baseUrl),
            vendorPayoutGateway: HttpVendorPayoutGateway(baseUrl: baseUrl),
            vendorTransactionGateway: HttpVendorTransactionGateway(baseUrl: baseUrl),
            vendorCrudGateway: HttpVendorCrudGateway(baseUrl: baseUrl)
        )
    }
}
