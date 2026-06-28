import Foundation

public protocol VendorTransactionGateway {
    func loadVendorTransaction(vendorId: String) async throws -> MobileVendorTransactionPayload
}
