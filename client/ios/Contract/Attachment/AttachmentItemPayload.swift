import Foundation

public struct AttachmentItemPayload: Sendable, Identifiable {
    public let id: String
    public let attachmentId: String
    public let type: String?
    public let mimeType: String?
    public let downloadUrl: String?
    public let payloadText: String?

    public init(attachmentId: String, type: String?, mimeType: String?, downloadUrl: String?, payloadText: String?) {
        self.id = attachmentId; self.attachmentId = attachmentId; self.type = type; self.mimeType = mimeType; self.downloadUrl = downloadUrl; self.payloadText = payloadText
    }
}
