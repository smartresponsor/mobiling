import Foundation

// Marketing America Corp. Oleksandr Tishchenko
protocol VendorListingGateway {
    func listVendors(query: ListVendorsQuery) async throws -> [VendorCardSummary]
}
