import Foundation

public struct MobileVendorProfileScreenContract: Sendable {
    public let vendorId: String
    public let title: String
    public let brandName: String?
    public let status: String?
    public let completionPercent: Int
    public let readyForPublishing: Bool
    public let nextAction: String?
    public let about: String?
    public let website: String?
    public let avatarUrl: String?
    public let coverUrl: String?
    public let publicationStatus: String?

    public init(payload: MobileVendorProfilePayload) {
        vendorId = payload.vendorId
        title = payload.displayName ?? payload.brandName ?? "My Profile"
        brandName = payload.brandName
        status = payload.status
        completionPercent = payload.completionPercent
        readyForPublishing = payload.readyForPublishing
        nextAction = payload.nextAction
        about = payload.about
        website = payload.website
        avatarUrl = payload.avatarUrl
        coverUrl = payload.coverUrl
        publicationStatus = payload.publicationStatus
    }
}
