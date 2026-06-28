import Foundation

public struct LoadVendorSummaryUseCase {
    private let gateway: VendorSummaryGateway

    public init(gateway: VendorSummaryGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(vendorId: String) async throws -> MobileVendorSummaryPayload {
        try await gateway.loadVendorSummary(vendorId: vendorId)
    }
}
