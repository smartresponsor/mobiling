import Foundation

public struct LoadVendorStatementUseCase {
    private let gateway: VendorStatementGateway

    public init(gateway: VendorStatementGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(vendorId: String) async throws -> MobileVendorStatementPayload {
        try await gateway.loadVendorStatement(vendorId: vendorId)
    }
}
