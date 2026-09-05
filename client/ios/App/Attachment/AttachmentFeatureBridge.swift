import Foundation

public struct AttachmentFeatureBridge: Sendable {
    private let reader: AttachmentReader
    private let writer: AttachmentWriter

    public init(reader: AttachmentReader, writer: AttachmentWriter) {
        self.reader = reader
        self.writer = writer
    }

    public func list(ownerType: String, ownerId: String, context: String? = nil, slot: String? = nil) async throws -> AttachmentListPayload {
        try await LoadAttachmentListUseCase(reader: reader)(ownerType: ownerType, ownerId: ownerId, context: context, slot: slot)
    }

    public func attach(request: AttachmentLinkRequest) async throws -> AttachmentLinkPayload {
        try await AttachAttachmentUseCase(writer: writer)(request: request)
    }

    public func fileHandoff(attachmentId: String) async throws -> AttachmentFileHandoffPayload {
        try await writer.fileHandoff(attachmentId: attachmentId)
    }

    public func uploadHandoff(request: AttachmentUploadHandoffRequest) async throws -> AttachmentUploadHandoffPayload {
        try await writer.uploadHandoff(request: request)
    }

    public func upload(request: AttachmentUploadHandoffRequest, fileName: String, mimeType: String, data: Data) async throws -> AttachmentItemPayload {
        try await writer.uploadAttachment(request: request, fileName: fileName, mimeType: mimeType, data: data)
    }
}
