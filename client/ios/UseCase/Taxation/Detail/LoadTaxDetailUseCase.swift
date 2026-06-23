import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct LoadTaxDetailUseCase {
    let gateway: TaxDetailGateway

    func callAsFunction(taxDocumentId: String) async throws -> TaxDetailPayload {
        try await gateway.loadTaxDetail(taxDocumentId: taxDocumentId)
    }
}
