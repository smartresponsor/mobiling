import Foundation

public struct AttachmentFileHandoffPayload: Sendable {
    public let attachmentId: String
    public let downloadUrl: String
    public let mimeType: String?
    public let fileName: String?
    public let handoffMode: String
    public let payloadText: String?

    public init(attachmentId: String, downloadUrl: String, mimeType: String?, fileName: String?, handoffMode: String, payloadText: String?) {
        self.attachmentId = attachmentId
        self.downloadUrl = downloadUrl
        self.mimeType = mimeType
        self.fileName = fileName
        self.handoffMode = handoffMode
        self.payloadText = payloadText
    }
}
