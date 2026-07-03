import Foundation

public protocol AttachmentWriter {
    func attachAttachment(request: AttachmentLinkRequest) async throws -> AttachmentLinkPayload
    func fileHandoff(attachmentId: String) async throws -> AttachmentFileHandoffPayload
    func uploadHandoff(request: AttachmentUploadHandoffRequest) async throws -> AttachmentUploadHandoffPayload
}
