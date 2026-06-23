import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct ListVendorsUseCase {
    let gateway: VendorListingGateway

    func callAsFunction(query: ListVendorsQuery) async throws -> [VendorCardSummary] {
        try await gateway.listVendors(query: query)
    }
}
