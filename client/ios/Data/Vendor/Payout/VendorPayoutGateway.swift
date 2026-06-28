import Foundation

public protocol VendorPayoutGateway {
    func loadVendorPayout(vendorId: String) async throws -> MobileVendorPayoutPayload
}
