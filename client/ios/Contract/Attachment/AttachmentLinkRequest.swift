import Foundation

public struct AttachmentLinkRequest: Sendable {
    public let attachmentId: Int64
    public let ownerType: String
    public let ownerId: String
    public let context: String?
    public let slot: String?
    public let position: Int?
    public let isPrimary: Bool?

    public init(attachmentId: Int64, ownerType: String, ownerId: String, context: String?, slot: String?, position: Int?, isPrimary: Bool?) {
        self.attachmentId = attachmentId; self.ownerType = ownerType; self.ownerId = ownerId; self.context = context; self.slot = slot; self.position = position; self.isPrimary = isPrimary
    }
}
