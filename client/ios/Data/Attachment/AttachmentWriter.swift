import Foundation

public protocol AttachmentWriter {
    func attachAttachment(request: AttachmentLinkRequest) async throws -> AttachmentLinkPayload
}
