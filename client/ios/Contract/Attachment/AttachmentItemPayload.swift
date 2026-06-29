import Foundation

public struct AttachmentItemPayload: Sendable, Identifiable {
    public let id: String
    public let attachmentId: Int64
    public let type: String?
    public let mimeType: String?
    public let downloadUrl: String?
    public let payloadText: String?

    public init(attachmentId: Int64, type: String?, mimeType: String?, downloadUrl: String?, payloadText: String?) {
        self.id = String(attachmentId); self.attachmentId = attachmentId; self.type = type; self.mimeType = mimeType; self.downloadUrl = downloadUrl; self.payloadText = payloadText
    }
}
