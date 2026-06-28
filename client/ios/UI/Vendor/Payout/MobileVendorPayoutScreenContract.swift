import Foundation

public struct MobileVendorPayoutScreenContract: Sendable {
    public let vendorId: String
    public let payoutStatus: String?
    public let currency: String?
    public let availableAmount: Double
    public let pendingAmount: Double
    public let payoutAccountLabel: String?

    public init(payload: MobileVendorPayoutPayload) { vendorId = payload.vendorId; payoutStatus = payload.payoutStatus; currency = payload.currency; availableAmount = payload.availableAmount; pendingAmount = payload.pendingAmount; payoutAccountLabel = payload.payoutAccountLabel }
}
