import Foundation

public struct AttachmentUploadHandoffRequest: Sendable {
    public let ownerType: String
    public let ownerId: String
    public let context: String?
    public let slot: String?
    public let isPrimary: Bool
    public let title: String?
    public let description: String?
    public let altText: String?

    public init(ownerType: String, ownerId: String, context: String?, slot: String?, isPrimary: Bool, title: String?, description: String?, altText: String?) {
        self.ownerType = ownerType
        self.ownerId = ownerId
        self.context = context
        self.slot = slot
        self.isPrimary = isPrimary
        self.title = title
        self.description = description
        self.altText = altText
    }
}
