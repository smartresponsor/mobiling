import Foundation

public struct AttachAttachmentUseCase {
    private let writer: AttachmentWriter

    public init(writer: AttachmentWriter) {
        self.writer = writer
    }

    public func callAsFunction(request: AttachmentLinkRequest) async throws -> AttachmentLinkPayload {
        try await writer.attachAttachment(request: request)
    }
}
