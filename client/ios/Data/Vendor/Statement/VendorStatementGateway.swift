import Foundation

public protocol VendorStatementGateway {
    func loadVendorStatement(vendorId: String) async throws -> MobileVendorStatementPayload
}
