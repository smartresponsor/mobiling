import Foundation

public struct AttachmentLinkPayload: Sendable {
    public let linkId: String?
    public let attachmentId: String
    public let ownerType: String
    public let ownerId: String
    public let context: String?
    public let slot: String?
    public let position: Int?
    public let isPrimary: Bool?
    public let payloadText: String?

    public init(
        linkId: String?,
        attachmentId: String,
        ownerType: String,
        ownerId: String,
        context: String?,
        slot: String?,
        position: Int?,
        isPrimary: Bool?,
        payloadText: String?
    ) {
        self.linkId = linkId
        self.attachmentId = attachmentId
        self.ownerType = ownerType
        self.ownerId = ownerId
        self.context = context
        self.slot = slot
        self.position = position
        self.isPrimary = isPrimary
        self.payloadText = payloadText
    }
