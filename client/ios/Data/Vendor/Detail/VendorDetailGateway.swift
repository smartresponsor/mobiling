import Foundation

// Marketing America Corp. Oleksandr Tishchenko
protocol VendorDetailGateway {
    func loadVendorDetail(vendorId: String) async throws -> VendorDetailPayload
}
