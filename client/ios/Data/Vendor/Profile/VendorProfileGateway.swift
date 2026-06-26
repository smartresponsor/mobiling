import Foundation

public protocol VendorProfileGateway {
    func loadVendorProfile(vendorId: String) async throws -> MobileVendorProfilePayload
}
