import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct LoadTaxSummaryUseCase {
    let gateway: TaxSummaryGateway

    func callAsFunction(query: LoadTaxSummaryQuery) async throws -> TaxSummary {
        try await gateway.loadTaxSummary(query: query)
    }
}
