import Foundation

// Marketing America Corp. Oleksandr Tishchenko
struct LoadVendorDetailUseCase {
    let gateway: VendorDetailGateway

    func callAsFunction(vendorId: String) async throws -> VendorDetailPayload {
        try await gateway.loadVendorDetail(vendorId: vendorId)
    }
}
