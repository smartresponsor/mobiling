import Foundation

// Marketing America Corp. Oleksandr Tishchenko
// App-level bridge for vendor-domain controlled rewire.
public struct VendorFeatureBridge: Sendable {
    private let listingGateway: any VendorListingGateway
    private let detailGateway: any VendorDetailGateway

    public init(
        listingGateway: any VendorListingGateway,
        detailGateway: any VendorDetailGateway
    ) {
        self.listingGateway = listingGateway
        self.detailGateway = detailGateway
    }

    public func list(query: ListVendorsQuery) async throws -> [VendorCardSummary] {
        try await ListVendorsUseCase(gateway: listingGateway)(query: query)
    }

    public func detail(vendorId: String) async throws -> VendorDetailPayload? {
        try await LoadVendorDetailUseCase(gateway: detailGateway)(vendorId: vendorId)
    }
}
