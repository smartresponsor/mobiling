import Foundation

public protocol VendorSummaryGateway {
    func loadVendorSummary(vendorId: String) async throws -> MobileVendorSummaryPayload
}
