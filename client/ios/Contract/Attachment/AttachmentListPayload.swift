import Foundation

public struct AttachmentListPayload: Sendable {
    public let ownerType: String
    public let ownerId: String
    public let count: Int
    public let items: [AttachmentItemPayload]
    public let payloadText: String?

    public init(ownerType: String, ownerId: String, count: Int, items: [AttachmentItemPayload], payloadText: String?) {
        self.ownerType = ownerType; self.ownerId = ownerId; self.count = count; self.items = items; self.payloadText = payloadText
    }
}
