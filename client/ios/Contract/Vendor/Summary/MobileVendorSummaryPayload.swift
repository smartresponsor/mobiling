import Foundation

public struct MobileVendorSummaryPayload: Sendable {
    public let vendorId: String
    public let brandName: String?
    public let status: String?
    public let profileCompletionPercent: Int
    public let nextAction: String?
}
