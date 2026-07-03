import Foundation

public struct AttachmentUploadHandoffPayload: Sendable {
    public let uploadUrl: String
    public let method: String
    public let fieldName: String
    public let handoffMode: String
    public let payloadText: String?

    public init(uploadUrl: String, method: String, fieldName: String, handoffMode: String, payloadText: String?) {
        self.uploadUrl = uploadUrl
        self.method = method
        self.fieldName = fieldName
        self.handoffMode = handoffMode
        self.payloadText = payloadText
    }
}
