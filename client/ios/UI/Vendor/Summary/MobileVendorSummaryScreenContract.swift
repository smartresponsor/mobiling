import Foundation

public struct MobileVendorSummaryScreenContract: Sendable {
    public let vendorId: String
    public let title: String
    public let brandName: String?
    public let status: String?
    public let profileCompletionPercent: Int
    public let nextAction: String?

    public init(payload: MobileVendorSummaryPayload) { vendorId = payload.vendorId; title = payload.brandName ?? "Vendor Summary"; brandName = payload.brandName; status = payload.status; profileCompletionPercent = payload.profileCompletionPercent; nextAction = payload.nextAction }
}
