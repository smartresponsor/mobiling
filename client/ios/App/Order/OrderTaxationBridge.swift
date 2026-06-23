import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// Order-owned taxation bridge.
//
// Active surface is gateway-first so the order-owned wrapper no longer depends on
// the flat TaxationFeatureBridge for runtime behavior. A compatibility initializer
// remains for transitional call sites.
public struct OrderTaxationBridge: Sendable {
    private let summaryGateway: any TaxSummaryGateway
    private let detailGateway: any TaxDetailGateway

    public init(
        summaryGateway: any TaxSummaryGateway,
        detailGateway: any TaxDetailGateway
    ) {
        self.summaryGateway = summaryGateway
        self.detailGateway = detailGateway
    }

    public init(feature: TaxationFeatureBridge) {
        self.summaryGateway = feature.summaryGatewayRef()
        self.detailGateway = feature.detailGatewayRef()
    }

    public func summary(query: LoadTaxSummaryQuery) async throws -> TaxSummary {
        try await LoadTaxSummaryUseCase(gateway: summaryGateway)(query: query)
    }

    public func detail(taxationId: String) async throws -> TaxDetailPayload {
        try await LoadTaxDetailUseCase(gateway: detailGateway)(taxationId: taxationId)
    }
}
