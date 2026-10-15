import Foundation

public protocol AttachmentWriter {
    func attachAttachment(request: AttachmentLinkRequest) async throws -> AttachmentLinkPayload
    func fileHandoff(attachmentId: String) async throws -> AttachmentFileHandoffPayload
    func uploadHandoff(request: AttachmentUploadHandoffRequest) async throws -> AttachmentUploadHandoffPayload
    func uploadAttachment(request: AttachmentUploadHandoffRequest, fileName: String, mimeType: String, data: Data) async throws -> AttachmentItemPayload
}
