import Foundation

public struct LoadVendorTransactionUseCase {
    private let gateway: VendorTransactionGateway

    public init(gateway: VendorTransactionGateway) {
        self.gateway = gateway
    }

    public func callAsFunction(vendorId: String) async throws -> MobileVendorTransactionPayload {
        try await gateway.loadVendorTransaction(vendorId: vendorId)
    }
}
