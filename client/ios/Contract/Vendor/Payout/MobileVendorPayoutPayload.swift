import Foundation

public struct MobileVendorPayoutPayload: Sendable {
    public let vendorId: String
    public let payoutStatus: String?
    public let currency: String?
    public let availableAmount: Double
    public let pendingAmount: Double
    public let payoutAccountLabel: String?
}
