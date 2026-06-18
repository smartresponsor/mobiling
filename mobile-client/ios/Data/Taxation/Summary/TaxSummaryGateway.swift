import Foundation

// Marketing America Corp. Oleksandr Tishchenko
protocol TaxSummaryGateway {
    func loadTaxSummary(query: LoadTaxSummaryQuery) async throws -> TaxSummary
}
