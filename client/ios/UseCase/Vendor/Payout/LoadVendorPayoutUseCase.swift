import Foundation

public struct LoadVendorPayoutUseCase {
    private let gateway: VendorPayoutGateway

    public init(gateway: VendorPayoutGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(vendorId: String) async throws -> MobileVendorPayoutPayload {
        try await gateway.loadVendorPayout(vendorId: vendorId)
    }
}
