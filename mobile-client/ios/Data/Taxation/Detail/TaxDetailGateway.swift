import Foundation

// Marketing America Corp. Oleksandr Tishchenko
protocol TaxDetailGateway {
    func loadTaxDetail(taxDocumentId: String) async throws -> TaxDetailPayload
}
