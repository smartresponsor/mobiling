import Foundation

public struct MobileVendorProfilePayload: Sendable {
    public let vendorId: String
    public let displayName: String?
    public let brandName: String?
    public let status: String?
    public let completionPercent: Int
    public let readyForPublishing: Bool
    public let nextAction: String?
    public let avatarUrl: String?
    public let coverUrl: String?
    public let about: String?
    public let website: String?
    public let publicationStatus: String?
}
