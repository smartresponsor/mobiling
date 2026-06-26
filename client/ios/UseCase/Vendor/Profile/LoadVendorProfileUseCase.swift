import Foundation

public struct LoadVendorProfileUseCase {
    private let gateway: VendorProfileGateway

    public init(gateway: VendorProfileGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(vendorId: String) async throws -> MobileVendorProfilePayload {
        try await gateway.loadVendorProfile(vendorId: vendorId)
    }
}
