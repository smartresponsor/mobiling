import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// App-level bridge for taxation-domain controlled rewire.
public struct TaxationFeatureBridge: Sendable {
    private let summaryGateway: any TaxSummaryGateway
    private let detailGateway: any TaxDetailGateway

    public init(
        summaryGateway: any TaxSummaryGateway,
        detailGateway: any TaxDetailGateway
    ) {
        self.summaryGateway = summaryGateway
        self.detailGateway = detailGateway
    }

    public func summary(query: LoadTaxSummaryQuery) async throws -> TaxSummary {
        try await LoadTaxSummaryUseCase(gateway: summaryGateway)(query: query)
    }

    public func detail(taxationId: String) async throws -> TaxDetailPayload {
        try await LoadTaxDetailUseCase(gateway: detailGateway)(taxationId: taxationId)
    }

    func summaryGatewayRef() -> any TaxSummaryGateway {
        summaryGateway
    }

    func detailGatewayRef() -> any TaxDetailGateway {
        detailGateway
    }
}
